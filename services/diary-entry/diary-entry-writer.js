'use strict';

class DiaryEntryWriter {
    /**
     * @param {import('../discord/discord-message-sender')} discordMessageSender
     * @param {import('../google/firestore/firestore-previous-dao')} firestorePreviousDao
     * @param {import('../google/firestore/firestore-user-dao')} firestoreUserDao
     * @param {import('../../factories/message-embed-factory')} messageEmbedFactory
     * @param {import('../subscribed-user-list')} subscribedUserList
     */
    constructor(
        discordMessageSender,
        firestorePreviousDao,
        firestoreUserDao,
        messageEmbedFactory,
        subscribedUserList,
    ) {
        this.discordMessageSender = discordMessageSender;
        this.firestorePreviousDao = firestorePreviousDao;
        this.firestoreUserDao = firestoreUserDao;
        this.messageEmbedFactory = messageEmbedFactory;
        this.subscribedUserList = subscribedUserList;
    }

    /**
     * @param {import('../../models/diary-entry')} diaryEntry
     * @param {string} channelIdOverride
     * @returns {Promise}
     */
    validateAndWrite(diaryEntry, channelIdOverride) {
        return new Promise((resolve) => {
            // Get the user data from cache
            const user = this.subscribedUserList.get(diaryEntry.userName);

            // Because we are expecting multiple requests to post a diary entry we maintain
            // the one source of truth on the server that sends messages so we double-check
            // the previous Id.
            // Ignore this check if there is a channel override because that will go no matter what.
            if (diaryEntry.id <= user.previousId && !channelIdOverride) {
                return resolve();
            }

            this.firestoreUserDao
                .read(diaryEntry.userName)
                .then((userData) => {
                    // Rewrite the channel list if there is an override sent
                    userData.channelList = channelIdOverride
                        ? [{ channelId: channelIdOverride }]
                        : userData.channelList;

                    // Exit early if no subscribed channels
                    if (userData.channelList.length === 0) {
                        return resolve();
                    }

                    // Get sender promise list with mapped failures to noops
                    const promiseList = this.createSenderPromiseList(diaryEntry, userData).map(
                        (p) => p.catch(() => false),
                    );
                    Promise.all(promiseList).then((results) => {
                        // If we weren't able to post any messages just move on.
                        if (results.filter(Boolean).length == 0) {
                            return resolve();
                        }

                        // At least one message posted, so update previous data in database and local cache
                        const entryId = diaryEntry.id;
                        if (this.subscribedUserList.upsert(userData.userName, entryId) == entryId) {
                            this.firestorePreviousDao.update(userData, diaryEntry);
                        }
                        return resolve();
                    });
                })
                .catch(() => {
                    // TODO: Log on user read failure or something
                    return resolve();
                });
        });
    }

    /**
     * @param {import("../../models/diary-entry")} diaryEntry
     * @param {import("../../models/user")} userData
     * @returns {Promise<boolean>[]}
     */
    createSenderPromiseList(diaryEntry, userData) {
        const permissionsPromiseList = userData.channelList.map((channel) => {
            return new Promise((resolve, reject) => {
                const message = this.messageEmbedFactory.createDiaryEntryMessage(
                    diaryEntry,
                    userData,
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

        return permissionsPromiseList;
    }
}

module.exports = DiaryEntryWriter;
