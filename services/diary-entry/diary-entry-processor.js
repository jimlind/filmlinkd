'use strict';

class DiaryEntryProcessor {
    /**
     *
     * @param {import('../Logger')} logger
     * @param {*} firestorePreviousDao
     * @param {*} messageEmbedFactory
     * @param {*} subscribedUserList
     */
    constructor(logger, firestorePreviousDao, messageEmbedFactory, subscribedUserList) {
        this.logger = logger;
        this.firestorePreviousDao = firestorePreviousDao;
        this.messageEmbedFactory = messageEmbedFactory;
        this.subscribedUserList = subscribedUserList;
    }

    /**
     *
     * @param {number} index
     * @param {number} pageSize
     * @returns {Promise<number>}
     */
    processPageOfEntries(index, pageSize) {
        return new Promise((resolve) => {
            this.subscribedUserList.getActiveSubscriptionsPage(index, pageSize).then((userList) => {
                // Special usecase for empty user list returned.
                if (!userList.length) {
                    this.logger.info('Empty page. Resetting pagination.');
                    return resolve(0);
                }

                const used = process.memoryUsage().heapUsed / 1024 / 1024;
                this.logger.info(
                    `Starting work on page of ${userList.length} records. [${used} MB]`,
                );

                let userListPostCount = 0;
                userList.forEach((user) => {
                    // // Post any entries available for the user
                    // this.postEntriesForUser(user, 10).then(() => {
                    //     userListPostCount++;
                    //     if (userList.length === userListPostCount) {
                    //         return resolve(userListPostCount);
                    //     }
                    // });
                });
            });
        });
    }
}

module.exports = DiaryEntryProcessor;
