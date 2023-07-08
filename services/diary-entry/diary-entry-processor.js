'use strict';

const pLimit = require('p-limit');

/**
 * Entry point for all diary entry logging work.
 * The API feed processor is used for all feed processors.
 */
class DiaryEntryProcessor {
    /**
     * @param {import('../diary-entry/diary-entry-publisher')} diaryEntryPublisher
     * @param {import('../letterboxd/letterboxd-lid-comparison')} letterboxdLidComparison
     * @param {import('../letterboxd/api/letterboxd-log-entry-api')} letterboxdLogEntryApi
     * @param {import('../logger.mjs')} logger
     * @param {import('../subscribed-user-list')} subscribedUserList
     */
    constructor(
        diaryEntryPublisher,
        letterboxdLidComparison,
        letterboxdLogEntryApi,
        logger,
        subscribedUserList,
    ) {
        this.diaryEntryPublisher = diaryEntryPublisher;
        this.letterboxdLidComparison = letterboxdLidComparison;
        this.letterboxdLogEntryApi = letterboxdLogEntryApi;
        this.logger = logger;
        this.subscribedUserList = subscribedUserList;
    }

    /**
     * @param {Object} userModel
     * @param {string} channelId
     */
    processMostRecentForUser(userModel, channelId) {
        this.getNewLogEntriesForUser(userModel.letterboxdId, '', 1).then(([logEntry]) => {
            // There is some slightly convoluted logic here.
            // We want to process the most recent entry for a user and post it BUT if that same most recent
            // entry isn't already posted on all the channels where the account is followed it could cause
            // issues where the post for a single channel is considered a global post and the database is
            // updated.
            // To attempt to compensate for this check if the entry that we find here from a fresh scrape is
            // the most recent diary entry. If it is same continue as usual. If it is new then unset the
            // channel value so that the entry will be sent to all channels.
            const previousLid = userModel.previous.lid;
            const isNew = this.letterboxdLidComparison.compare(logEntry.id, previousLid) == 1;
            channelId = isNew ? '' : channelId;

            return this.diaryEntryPublisher.publishLogEntryList([logEntry], channelId);
        });
    }

    /**
     * @param {number} index
     * @param {number} pageSize
     * @returns {Promise<number>}
     */
    processPageOfEntries(index, pageSize) {
        const getUserPage = this.subscribedUserList.getActiveSubscriptionsPage(index, pageSize);

        return getUserPage
            .then((userList) => {
                if (userList.length == 0) {
                    this.logger.info('Returning empty page of normal users. Should reset.');
                    return [];
                }

                const limit = pLimit(4);
                const promiseList = userList.map((user) =>
                    limit(() => this.getNewLogEntriesForUser(user.userLid, user.entryLid, 10)),
                );

                return Promise.all(promiseList);
            })
            .then((logEntryCollection) => {
                const limit = pLimit(1);
                const promiseList = logEntryCollection.map((userLogEntryList) => {
                    return limit(() => {
                        if (userLogEntryList.length === 0) {
                            return Promise.all([]);
                        }

                        const logEntry = userLogEntryList[0];
                        const message = `Publishing ${userLogEntryList.length}x films from "${logEntry.owner.userName}"`;
                        this.logger.info(message);

                        return this.diaryEntryPublisher.publishLogEntryList(userLogEntryList);
                    });
                });

                return Promise.all(promiseList);
            })
            .then((successList) => {
                const limit = pLimit(1);
                const promiseList = successList.flat().map((success) => {
                    return limit(() => {
                        return new Promise((resolve) => {
                            this.subscribedUserList.upsert(success.userLid, success.entryLid);
                            resolve(success.entryLid);
                        });
                    });
                });

                return Promise.all(promiseList);
            })
            .then(() => getUserPage)
            .then((userList) => userList.length)
            .catch(() => 0);
    }

    /**
     * @param {number} index
     * @param {number} pageSize
     * @returns {Promise<number>}
     */
    processPageOfVipEntries(index, pageSize) {
        const getVipPage = this.subscribedUserList.getActiveVipSubscriptionsPage(index, pageSize);

        return getVipPage
            .then((userList) => {
                if (userList.length == 0) {
                    this.logger.info('Returning empty page of VIP users. Should reset.');
                    return [];
                }

                const limit = pLimit(4);
                const promiseList = userList.map((user) =>
                    limit(() => this.getNewLogEntriesForUser(user.userLid, user.entryLid, 10)),
                );

                return Promise.all(promiseList);
            })
            .then((logEntryCollection) => {
                const limit = pLimit(1);
                const promiseList = logEntryCollection.map((userLogEntryList) => {
                    return limit(() => {
                        if (userLogEntryList.length === 0) {
                            return Promise.all([]);
                        }

                        const logEntry = userLogEntryList[0];
                        const message = `Publishing ${userLogEntryList.length}x films from VIP "${logEntry.owner.userName}"`;
                        this.logger.info(message);

                        return this.diaryEntryPublisher.publishLogEntryList(userLogEntryList);
                    });
                });

                return Promise.all(promiseList);
            })
            .then((successList) => {
                const limit = pLimit(1);
                const promiseList = successList.flat().map((success) => {
                    return limit(() => {
                        return new Promise((resolve) => {
                            this.subscribedUserList.upsertVip(success.userLid, success.entryLid);
                            resolve(success.entryLid);
                        });
                    });
                });

                return Promise.all(promiseList);
            })
            .then(() => getVipPage)
            .then((vipUserList) => Object.entries(vipUserList).length)
            .catch(() => 0);
    }

    /**
     * Get a list of any new log entries based on User LID
     *
     * Test to make sure everything still works.
     *
     * @param {string} userLid
     * @param {string} entryLid
     * @param {number} maxDiaryEntries
     * @returns {Promise<import('../../models/letterboxd/letterboxd-log-entry')[]>}
     */
    getNewLogEntriesForUser(userLid, entryLid, maxDiaryEntries) {
        return this.letterboxdLogEntryApi
            .getByMember(userLid, maxDiaryEntries)
            .then((logEntryList) => {
                return logEntryList.filter((logEntry) => {
                    // Filter out entries that are less than 3 minutes old
                    if (Date.now() - Date.parse(logEntry.whenCreated) < 180000) {
                        return false;
                    }

                    // Filter out entries that are older than the current
                    const x = this.letterboxdLidComparison.compare(entryLid, logEntry.id);
                    return x === 1;
                });
            })
            .catch(() => {
                const message = `Error getting entries from User LID ${userLid} via Entry LID`;
                this.logger.warn(message);
                return [];
            });
    }
}

module.exports = DiaryEntryProcessor;
