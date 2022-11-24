'use strict';

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
     * @param {string} userName
     * @param {string} channelId
     */
    processMostRecentForUser(userName, channelId) {
        const user = { userName, previousId: 0 };
        this.getNewEntriesForUser(user, 1).then((diaryEntryList) => {
            this.diaryEntryPublisher.publish(diaryEntryList, channelId);
        });
    }

    /**
     * @param {number} index
     * @param {number} pageSize
     * @returns {Promise<number>}
     */
    processPageOfEntries(index, pageSize) {
        return new Promise((resolve) => {
            this.subscribedUserList.getActiveSubscriptionsPage(index, pageSize).then((userList) => {
                // Special usecase for empty user list returned.
                if (!userList.length) {
                    this.logger.info('Empty page of users. Resetting pagination.');
                    return resolve(0);
                }

                // Loop over the users and with a slight delay between each request
                let userListPostCount = 0;
                userList.forEach((user, userIndex) => {
                    setTimeout(() => {
                        this.getNewEntriesForUser(user, 10).then((diaryEntryList) => {
                            this.diaryEntryPublisher.publish(diaryEntryList);
                        });
                        userListPostCount++;
                        if (userList.length === userListPostCount) {
                            return resolve(userListPostCount);
                        }
                    }, userIndex * 100);
                });
            });
        });
    }

    processPageOfVipEntries(index, pageSize) {
        return new Promise((resolve) => {
            this.subscribedUserList
                .getActiveVipSubscriptionsPage(index, pageSize)
                .then((vipUserList) => {
                    const userEntryList = Object.entries(vipUserList);
                    const userList = userEntryList.map((d) => ({ userLid: d[0], ...d[1] }));

                    if (!userList.length) {
                        return resolve(0); // If user list is empty exit
                    }

                    // Create list of promises with noop failures
                    const entryPromiseList = userList
                        .map((user) => this.getNewLogEntriesForUser(user, 10))
                        .map((p) => p.catch(() => false));

                    Promise.all(entryPromiseList).then((values) => {
                        values.flat().forEach((logEntry) => {
                            if (typeof logEntry == 'boolean') return; // Enforce typing (sort of)

                            const message = `Publishing "${logEntry.film.name}" from VIP "${logEntry.owner.userName}"`;
                            this.logger.info(message);

                            this.diaryEntryPublisher
                                .publishLogEntryList([logEntry])
                                .then((successList) => {
                                    successList.forEach((success) => {
                                        this.subscribedUserList.upsertVip(
                                            success.userLid,
                                            success.entryId,
                                            success.entryLid,
                                        );
                                    });
                                });
                        });

                        return resolve(userList.length);
                    });
                });
        });
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
     * @param {{ userLid: string; entryId: number; entryLid: string; }} user
     * @param {number} maxDiaryEntries
     * @returns {Promise<import('../../models/letterboxd/letterboxd-log-entry')[]>}
     */
    getNewLogEntriesForUser(user, maxDiaryEntries) {
        if (user.entryLid) {
            // Use Entry LID to get newest entries
            return this.letterboxdLogEntryApi
                .getByMember(user.userLid, maxDiaryEntries)
                .then((logEntryList) => {
                    return logEntryList.filter((logEntry) => {
                        const x = this.letterboxdLidComparison.compare(user.entryLid, logEntry.id);
                        return x === 1;
                    });
                })
                .catch(() => {
                    const message = `Error getting entries from User LID ${user.userLid} via Entry LID`;
                    this.logger.warn(message);
                    return [];
                });
        }

        // This is the older slower way to process this data. The RSS feed had a "View ID" number that
        // the API doesn't have because. Luckily I can grab that value from the web. Unluckily there isn't
        // a way to only get some of those values so I burn bandwidth getting all of them.
        //
        // I'll be able to delete this eventually.
        const entryPromise = this.letterboxdLogEntryApi.getByMember(user.userLid, maxDiaryEntries);
        const createViewIdPromiseList = (logEntryList) => {
            const promiseList = logEntryList.map((logEntry) => {
                const getLetterboxdUrl = (prev, cur) => (cur.type == 'letterboxd' ? cur.url : prev);
                return this.letterboxdViewingIdWeb.get(logEntry.links.reduce(getLetterboxdUrl, ''));
            });
            return Promise.all(promiseList);
        };
        const promiseList = [entryPromise, entryPromise.then(createViewIdPromiseList)];

        return Promise.all(promiseList)
            .then((returnValues) => {
                const entryList = returnValues?.[0] || [];
                const viewingIdList = returnValues?.[1] || [];

                return entryList.filter((entry, index) => {
                    const viewingId = viewingIdList?.[index];
                    // Adding the viewingId here is extremely hacky
                    // I don't want this model to have this value forever and it doesn't follow the API spec
                    entry.viewingId = viewingId;
                    return viewingId && viewingId > user.entryId;
                });
            })
            .catch(() => {
                this.logger.warn(`Could not use Letterboxd API for ${user.userLid}`);
                return [];
            });
    }
}

module.exports = DiaryEntryProcessor;
