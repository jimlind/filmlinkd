'use strict';

class DiaryEntryWriter {
    /**
     * @param {import('../discord/discord-message-sender')} discordMessageSender
     * @param {import('../google/firestore/firestore-user-dao')} firestoreUserDao
     * @param {import('../../factories/message-embed-factory')} messageEmbedFactory
     * @param {import('../subscribed-user-list')} subscribedUserList
     */
    constructor(discordMessageSender, firestoreUserDao, messageEmbedFactory, subscribedUserList) {
        this.discordMessageSender = discordMessageSender;
        this.firestoreUserDao = firestoreUserDao;
        this.messageEmbedFactory = messageEmbedFactory;
        this.subscribedUserList = subscribedUserList;
    }

    /**
     * @param {import('../../models/diary-entry')} diaryEntry
     * @returns {Promise}
     */
    validateAndWrite(diaryEntry) {
        return new Promise((resolve, reject) => {
            const user = this.subscribedUserList.get(diaryEntry.userName);

            // Because we are expecting multiple requests to post a diary entry we maintain
            // the one source of truth on the server that sends messages so we double check
            // the previous Id.
            if (user && diaryEntry.id <= user.previousId) {
                return resolve(); // Exit early if the diary entry is latest
            }

            this.firestoreUserDao.read(diaryEntry.userName).then((userData) => {
                if (userData.channelList === 0) {
                    return resolve(); // Exit early if no subscribed channels
                }

                const promiseList = this.createSenderPromiseList(diaryEntry, userData).map((p) =>
                    p.catch(() => false),
                );
                Promise.all(promiseList).then((results) => {
                    const sentMessageCount = results.filter(Boolean).length;
                    if (sentMessageCount) {
                        console.log('LOG THIS AS A SUCCESS');
                        // Message published successful so update previous data in database and local cache

                        /*
                        this.firestoreUserDao
                            .read(user.userName)
                            .then((userData) => {
                        */
                        //this.firestorePreviousDao.update(userData, diaryEntry);
                        this.subscribedUserList.upsert(userData.userName, diaryEntry.id);
                    }
                });
            });
        });
    }

    /**
     * @param {import("../../models/diary-entry")} diaryEntry
     * @param {any} userData
     * @returns {Promise<boolean>[]}
     */
    createSenderPromiseList(diaryEntry, userData) {
        const permissionsPromiseList = userData.channelList.map((channel) => {
            return new Promise((resolve, reject) => {
                this.discordMessageSender
                    .getPermissions(channel.channelId)
                    .then((permissions) => {
                        const message = this.messageEmbedFactory.createDiaryEntryMessage(
                            diaryEntry,
                            userData,
                            permissions,
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
                    })
                    .catch(() => {
                        // Failure getting permissions
                        reject();
                    });
            });
        });

        return permissionsPromiseList;
    }
}

module.exports = DiaryEntryWriter;
