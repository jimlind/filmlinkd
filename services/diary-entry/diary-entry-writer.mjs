/**
 * Class dealing with writing diary entry events to Discord servers
 */
export default class DiaryEntryWriter {
    skipUserNotFound = 'SKIP_USER_NOT_FOUND';
    skipOldDiaryEntry = 'SKIP_OLD_DIARY_ENTRY';
    skipEmptyChannelList = 'SKIP_EMPTY_CHANNEL_LIST';
    skipAdultFilm = 'SKIP_ADULT_FILM';
    skipNoMessagesSent = 'SKIP_NO_MESSAGES_SENT';
    /**
     * @type {{}}
     */
    cachedPreviousData = {};

    /**
     * @param {import('../discord/discord-message-sender.mjs')} discordMessageSender
     * @param {import('../../factories/embed-builder-factory.mjs')} embedBuilderFactory
     * @param {import('../google/firestore/firestore-user-dao.mjs')} firestoreUserDao
     * @param {import('../letterboxd/letterboxd-viewing-id-web.mjs')} letterboxdViewingIdWeb
     * @param {import('../logger.mjs')} logger
     * @param {import('../google/pubsub/pub-sub-connection.mjs')} pubSubConnection
     */
    constructor(
        discordMessageSender,
        embedBuilderFactory,
        firestoreUserDao,
        letterboxdViewingIdWeb,
        logger,
        pubSubConnection,
    ) {
        this.discordMessageSender = discordMessageSender;
        this.embedBuilderFactory = embedBuilderFactory;
        this.firestoreUserDao = firestoreUserDao;
        this.letterboxdViewingIdWeb = letterboxdViewingIdWeb;
        this.logger = logger;
        this.pubSubConnection = pubSubConnection;
    }

    /**
     * @param {import('../../models/diary-entry.mjs')} diaryEntry
     * @param {string} channelIdOverride
     * @returns {Promise}
     */
    validateAndWrite(diaryEntry, channelIdOverride) {
        // getViewingId is a Promise so we can access data or call another promise
        const getViewingId = new Promise((resolve) => {
            if (diaryEntry.id) {
                resolve(diaryEntry.id);
            }
            this.letterboxdViewingIdWeb
                .get(diaryEntry.link)
                .then((id) => {
                    resolve(id);
                })
                .catch(() => {
                    resolve('0');
                });
        });

        // getUserModel is a Promise so duplicate calls to the Dao aren't made
        const getUserModel = new Promise((resolve) => {
            this.firestoreUserDao
                .getByUserName(diaryEntry.userName)
                .then((model) => {
                    resolve(model);
                })
                .catch(() => {
                    resolve(null);
                });
        });

        return getViewingId
            .then((viewingId) => {
                // We are expecting multiple requests to post a diary entry so we maintain
                // the one source of truth on the server that sends messages. We keep a local cache current.
                const cleanViewingId = parseInt(viewingId);
                const previousList = this.cachedPreviousData[diaryEntry.userName] || [];

                // Ignore this check if there is a channel override because we want it to trigger multiple times.
                if (!channelIdOverride && previousList.includes(cleanViewingId)) {
                    throw this.skipOldDiaryEntry;
                }
                previousList.push(cleanViewingId);
                this.cachedPreviousData[diaryEntry.userName] = previousList.sort().slice(-10);

                return Promise.all([getUserModel, getViewingId]);
            })
            .then(([userModel, viewingId]) => {
                // Exit early if user not found
                if (!userModel) {
                    throw this.skipUserNotFound;
                }

                // Exit early if no subscribed channels
                if ((userModel?.channelList || []).length === 0) {
                    throw this.skipEmptyChannelList;
                }

                // Exit early if it is an adult film (maybe a future feature)
                if (diaryEntry?.adult) {
                    throw this.skipAdultFilm;
                }

                // Double check that the entry is newer than what was stored in the database
                // Ignore this check if there is a channel override because we want it to trigger multiple times.
                if (!channelIdOverride && (userModel?.previous?.id || 0) >= viewingId) {
                    throw this.skipOldDiaryEntry;
                }

                // Rewrite the channel list if there is an override sent
                const channelList = [{ channelId: channelIdOverride }];
                const sendingUser = channelIdOverride ? { ...userModel, channelList } : userModel;

                // Get sender promise list with mapped failures to no-ops
                return this.createSenderPromise(diaryEntry, sendingUser);
            })
            .then((senderResultList) => {
                // If we weren't able to post any messages just move on.
                if (senderResultList.filter(Boolean).length == 0) {
                    throw this.skipNoMessagesSent;
                }
                // Pass some worthwhile data to the promise reciever.
                // Probably too much data if I can be honest, but for now
                // this is better than it used to be.
                return Promise.all([getUserModel, getViewingId]);
            })
            .catch((error) => {
                // Don't log any of the normal rejection reasons, these are already logged.
                const allowedErrorList = [
                    this.skipUserNotFound,
                    this.skipOldDiaryEntry,
                    this.skipEmptyChannelList,
                    this.skipAdultFilm,
                    this.skipNoMessagesSent,
                ];
                if (allowedErrorList.includes(error)) {
                    return [false, false];
                }

                const logData = { error, diaryEntry, channelIdOverride };
                this.logger.warn(
                    `Entry for '${diaryEntry?.filmTitle}' by '${diaryEntry?.userName}' did not validate and write.`,
                    logData,
                );
                return [false, false];
            });
    }

    /**
     * @param {import("../../models/diary-entry.mjs")} diaryEntry
     * @param {import("../../models/user.mjs")} userModel
     * @returns {Promise<boolean[]>}
     */
    createSenderPromise(diaryEntry, userModel) {
        try {
            var embed = this.embedBuilderFactory.createDiaryEntryEmbed(diaryEntry, userModel);
        } catch (error) {
            const message = `Creating Diary Entry Embed for '${diaryEntry?.filmTitle}' by '${diaryEntry?.userName}' failed.`;
            this.logger.warn(message);
            return Promise.all([]);
        }

        const sendPromiseList = (userModel?.channelList || []).map((channel) => {
            return this.discordMessageSender
                .send(channel.channelId, embed)
                .then(() => true)
                .catch(() => false);
        });
        return Promise.all(sendPromiseList);
    }
}
