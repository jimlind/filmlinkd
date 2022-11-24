'use strict';

/**
 * Class dealing with writing diary entry events to Discord servers
 */
class DiaryEntryWriter {
    /**
     * @param {import('../discord/discord-message-sender')} discordMessageSender
     * @param {import('../google/firestore/firestore-previous-dao')} firestorePreviousDao
     * @param {import('../google/firestore/firestore-user-dao')} firestoreUserDao
     * @param {import('../letterboxd/letterboxd-viewing-id-web')} letterboxdViewingIdWeb
     * @param {import('../logger')} logger
     * @param {import('../../factories/message-embed-factory')} messageEmbedFactory
     * @param {import('../subscribed-user-list')} subscribedUserList
     */
    constructor(
        discordMessageSender,
        firestorePreviousDao,
        firestoreUserDao,
        letterboxdViewingIdWeb,
        logger,
        messageEmbedFactory,
        subscribedUserList,
    ) {
        this.discordMessageSender = discordMessageSender;
        this.firestorePreviousDao = firestorePreviousDao;
        this.firestoreUserDao = firestoreUserDao;
        this.letterboxdViewingIdWeb = letterboxdViewingIdWeb;
        this.logger = logger;
        this.messageEmbedFactory = messageEmbedFactory;
        this.subscribedUserList = subscribedUserList;
    }

    /**
     * @param {import('../../models/diary-entry')} diaryEntry
     * @param {string} channelIdOverride
     * @returns {Promise}
     */
    validateAndWrite(diaryEntry, channelIdOverride) {
        const noopPromise = new Promise(() => {});
        const getViewingId = new Promise((resolve) => {
            resolve(diaryEntry.id || this.letterboxdViewingIdWeb.get(diaryEntry.link));
        });
        // Here getUserModel is wrapped in a Promise so duplicate calls to the Dao aren't made
        const getUserModel = new Promise((resolve) => {
            resolve(this.firestoreUserDao.getByUserName(diaryEntry.userName));
        });

        return getViewingId
            .then((viewingId) => {
                // Get the user data from cache
                const user = this.subscribedUserList.get(diaryEntry.userName);

                // Because we are expecting multiple requests to post a diary entry we maintain
                // the one source of truth on the server that sends messages so we double-check
                // the previous Id.
                // Ignore this check if there is a channel override because we want it to trigger multiple times.
                if (viewingId <= user.previousId && !channelIdOverride) {
                    return noopPromise;
                }
                return getUserModel;
            })
            .then((userModel) => {
                // Exit early if no subscribed channels
                if (userModel.channelList.length === 0) {
                    return noopPromise;
                }

                // Exit early if it is an adult film (maybe a future feature)
                if (diaryEntry.adult) {
                    return noopPromise;
                }

                // Rewrite the channel list if there is an override sent
                const channelList = [{ channelId: channelIdOverride }];
                const sendingUser = channelIdOverride ? { ...userModel, channelList } : userModel;

                // Get sender promise list with mapped failures to noops
                return this.createSenderPromise(diaryEntry, sendingUser);
            })
            .then((senderResultList) => {
                // If we weren't able to post any messages just move on.
                if (senderResultList.filter(Boolean).length == 0) {
                    return noopPromise;
                }
                return Promise.all([getUserModel, getViewingId]);
            })
            .then((resultList) => {
                const [userModel, viewingId] = resultList;
                diaryEntry.id = viewingId;
                // At least one message posted, so update previous data in database and local cache
                const upsertResult = this.subscribedUserList.upsert(
                    userModel.userName,
                    userModel.letterboxdId,
                    diaryEntry.id,
                );
                if (upsertResult == diaryEntry.id) {
                    this.firestorePreviousDao.update(userModel, diaryEntry);
                }
            })
            .catch(() => {
                this.logger.warn(
                    `Entry for "${diaryEntry.filmTitle}" by "${diaryEntry.userName}" failed to validate and write.`,
                );
            });
    }

    /**
     * @param {import("../../models/diary-entry")} diaryEntry
     * @param {import("../../models/user")} userModel
     * @returns {Promise<boolean[]>}
     */
    createSenderPromise(diaryEntry, userModel) {
        const sendPromiseList = userModel.channelList.map((channel) => {
            return new Promise((resolve, reject) => {
                const message = this.messageEmbedFactory.createDiaryEntryMessage(
                    diaryEntry,
                    userModel,
                );
                this.discordMessageSender
                    .send(channel.channelId, message)
                    .then(() => {
                        // Successfully posted message
                        return resolve(true);
                    })
                    .catch(() => {
                        // Failure posting message
                        return reject();
                    });
            });
        });

        const cleanedPromiseList = sendPromiseList.map((p) => p.catch(() => false));
        return Promise.all(cleanedPromiseList);
    }
}

module.exports = DiaryEntryWriter;
