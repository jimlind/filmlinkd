'use strict';

class DiaryEntryWriter {
    constructor(
        discordMessageSender,
        firestorePreviousDao,
        messageEmbedFactory,
        subscribedUserList,
    ) {
        this.discordMessageSender = discordMessageSender;
        this.firestorePreviousDao = firestorePreviousDao;
        this.messageEmbedFactory = messageEmbedFactory;
        this.subscribedUserList = subscribedUserList;
    }

    /**
     * @param {import('../../models/diary-entry')} diaryEntry
     * @returns {Promise}
     */
    validateAndWrite(diaryEntry) {
        return new Promise((resolve) => {
            this.firestoreUserDao
                        .read(user.userName)
                        .then((userData) => {
                            if (userData.channelList === 0) {
                                return resolve(); // Exit early if no subscribed channels
                            }
            return resolve();
        });
    }
}

module.exports = DiaryEntryWriter;
