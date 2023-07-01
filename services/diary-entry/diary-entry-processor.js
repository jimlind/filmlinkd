'use strict';

const pLimit = require('p-limit');

/**
 * Entry point for all diary entry logging work.
 * The RSS feed processor (new entries) is used by the standard feed processor.
 * The API feed processor (new log entries) is used by the vip feed processor.
 */
class DiaryEntryProcessor {
    /**
     * @param {import('../diary-entry/diary-entry-publisher')} diaryEntryPublisher
     * @param {import('../letterboxd/letterboxd-diary-rss')} letterboxdDiaryRss
     * @param {import('../letterboxd/letterboxd-lid-comparison')} letterboxdLidComparison
     * @param {import('../letterboxd/letterboxd-likes-web')} letterboxdLikesWeb
     * @param {import('../letterboxd/api/letterboxd-log-entry-api')} letterboxdLogEntryApi
     * @param {import('../letterboxd/letterboxd-viewing-id-web')} letterboxdViewingIdWeb
     * @param {import('../logger')} logger
     * @param {import('../subscribed-user-list')} subscribedUserList
     */
    constructor(
        diaryEntryPublisher,
        letterboxdDiaryRss,
        letterboxdLidComparison,
        letterboxdLikesWeb,
        letterboxdLogEntryApi,
        letterboxdViewingIdWeb,
        logger,
        subscribedUserList,
    ) {
        this.diaryEntryPublisher = diaryEntryPublisher;
        this.letterboxdDiaryRss = letterboxdDiaryRss;
        this.letterboxdLidComparison = letterboxdLidComparison;
        this.letterboxdLikesWeb = letterboxdLikesWeb;
        this.letterboxdLogEntryApi = letterboxdLogEntryApi;
        this.letterboxdViewingIdWeb = letterboxdViewingIdWeb;
        this.logger = logger;
        this.subscribedUserList = subscribedUserList;
    }

    /**
     * @param {Object} userModel
     * @param {string} channelId
     */
    processMostRecentForUser(userModel, channelId) {
        const user = { userName: userModel.userName, previousId: 0 };
        this.getNewEntriesForUser(user, 1).then((diaryEntryList) => {
            // There is some slightly convoluted logic here.
            // We want to process the most recent entry for a user and post it BUT if that same most recent
            // entry isn't already posted on all the channels where the account is followed it could cause
            // issues where the post for a single channel is considered a global post and the database is
            // updated.
            // To attempt to compensate for this check if the entry that we find here from a fresh scrape is
            // the most recent diary entry. If it is same continue as usual. If it is new then unset the
            // channel value so that the entry will be sent to all channels.
            const diaryEntryId = diaryEntryList[0]?.id || 0;
            const previousDiaryEntryId = userModel?.previous?.id || 0;
            channelId = diaryEntryId > previousDiaryEntryId ? '' : channelId;

            this.diaryEntryPublisher.publish(diaryEntryList, channelId);
        });
    }

    /**
     * @param {number} index
     * @param {number} pageSize
     * @returns {Promise<number>}
     */
    processPageOfEntries(index, pageSize) {
        return this.subscribedUserList
            .getActiveSubscriptionsPage(index, pageSize)
            .then((userList) => {
                if (!userList.length) {
                    this.logger.info('Returning empty page of normal users. Should reset.');
                    return Promise.all([]);
                }
                const promiseList = userList.map((user) => {
                    // Limit to 10 entries
                    return this.getNewEntriesForUser(user, 10).then((diaryEntryList) => {
                        this.diaryEntryPublisher.publish(diaryEntryList);
                        return { userName: user.userName, userLid: user.lid, diaryEntryList };
                    });
                });
                return Promise.all(promiseList);
            })
            .then((results) => {
                // Update local cache of previous entry id
                results.forEach((result) => {
                    result.diaryEntryList.forEach((entry) => {
                        this.subscribedUserList.upsert(result.userName, result.userLid, entry.id);
                    });
                });
                return results.length;
            });
    }

    /**
     * @param {number} index
     * @param {number} pageSize
     * @returns {Promise<number>}
     */
    processPageOfVipEntries(index, pageSize) {
        const getVipPage = this.subscribedUserList.getActiveVipSubscriptionsPage(index, pageSize);

        return getVipPage
            .then((vipUserList) => {
                if (Object.keys(vipUserList).length == 0) {
                    this.logger.info('Returning empty page of VIP users. Should reset.');
                    return [];
                }

                const limit = pLimit(4);
                const promiseList = Object.entries(vipUserList).map((data) =>
                    limit(() => this.getNewLogEntriesForUser(data[0], data[1], 10)),
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
     * @param {{ userName: string; previousId: number; }} user
     * @param {number} maxDiaryEntries
     * @returns {Promise<import('../../models/diary-entry')[]>}
     */
    getNewEntriesForUser(user, maxDiaryEntries) {
        return new Promise((resolve) => {
            this.letterboxdDiaryRss
                .get(user.userName, maxDiaryEntries)
                .then((diaryEntryList) => {
                    let filteredDiaryEntryList = diaryEntryList.filter((diaryEntry) => {
                        // Include any entry newer than last logged
                        return diaryEntry.id > user.previousId;
                    });

                    // If there aren't any diary entries exit here
                    if (filteredDiaryEntryList.length == 0) {
                        return resolve([]);
                    }

                    // Collect list of all URLs for diary entries and get likes
                    const linkList = filteredDiaryEntryList.map((diaryEntry) => diaryEntry.link);
                    this.letterboxdLikesWeb
                        .get(user.userName, linkList)
                        .then((likedFilmLinkList) => {
                            // Add liked data to the list of diary entries
                            filteredDiaryEntryList = filteredDiaryEntryList.map((value) => {
                                const liked = likedFilmLinkList.filter(
                                    (filmLink) => value.link == filmLink,
                                );
                                value.liked = Boolean(liked.length);
                                return value;
                            });
                        })
                        .catch(() => {
                            this.logger.warn(
                                `Could not fetch film likes for "${user.userName}" on Letterboxd`,
                            );
                        })
                        .finally(() => {
                            return resolve(filteredDiaryEntryList);
                        });
                })
                .catch(() => {
                    this.logger.warn(`Could not fetch feed for "${user.userName}" on Letterboxd`);
                    return resolve([]);
                });
        });
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
