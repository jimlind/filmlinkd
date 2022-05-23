'use strict';

class DiaryEntryProcessor {
    /**
     * @param {import('../diary-entry/diary-entry-publisher')} diaryEntryPublisher
     * @param {import('../letterboxd/letterboxd-diary-rss')} letterboxdDiaryRss
     * @param {import('../letterboxd/letterboxd-lid-web')}letterboxdLidWeb
     * @param {import('../letterboxd/letterboxd-likes-web')} letterboxdLikesWeb
     * @param {import('../letterboxd/api/letterboxd-log-entry-api')} letterboxdLogEntryApi
     * @param {import('../letterboxd/letterboxd-viewing-id-web')} letterboxdViewingIdWeb
     * @param {import('../logger')} logger
     * @param {import('../subscribed-user-list')} subscribedUserList
     */
    constructor(
        diaryEntryPublisher,
        letterboxdDiaryRss,
        letterboxdLidWeb,
        letterboxdLikesWeb,
        letterboxdLogEntryApi,
        letterboxdViewingIdWeb,
        logger,
        subscribedUserList,
    ) {
        this.diaryEntryPublisher = diaryEntryPublisher;
        this.letterboxdDiaryRss = letterboxdDiaryRss;
        this.letterboxdLidWeb = letterboxdLidWeb;
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
                .then((userList) => {
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
                                        this.subscribedUserList.upsert(
                                            success.user,
                                            success.memberLetterboxdId,
                                            success.id,
                                            true,
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
     * This method is a little chaotic because we don't know how to compare api review ids to
     * the rss review ids so we have to do a additional look-ups against the viewing ids that
     * are availabe on the letterboxd review header
     *
     * @param {{ userName: string; lid: string; previousId: number; }} user
     * @param {number} maxDiaryEntries
     * @returns {Promise<import('../../models/letterboxd/letterboxd-log-entry')[]>}
     */
    getNewLogEntriesForUser(user, maxDiaryEntries) {
        const logEntryApiPromise = this.letterboxdLogEntryApi.getByMember(
            user.lid,
            maxDiaryEntries,
        );
        const createViewIdPromiseList = (logEntryList) => {
            const entryPromiseList = logEntryList.map((logEntry) => {
                const url = logEntry.links.reduce(
                    (previous, current) => (current.type == 'letterboxd' ? current.url : previous),
                    '',
                );
                return this.letterboxdViewingIdWeb.get(url);
            });
            return Promise.all(entryPromiseList);
        };

        const promiseList = [logEntryApiPromise, logEntryApiPromise.then(createViewIdPromiseList)];
        return Promise.all(promiseList)
            .then((returnValues) => {
                const entryList = returnValues?.[0] || [];
                const viewingIdList = returnValues?.[1] || [];
                const newEntryList = [];

                viewingIdList.forEach((viewingId, index) => {
                    if (viewingId > user.previousId) {
                        const newEntry = entryList?.[index];
                        // TODO: Delete Viewing Id. This is just temporarily being stored here.
                        newEntry.viewingId = viewingId;
                        newEntryList.push(newEntry);
                    }
                });

                return newEntryList;
            })
            .catch(() => {
                this.logger.warn(`Could not use Letterboxd API for ${user.lid}`);
                return [];
            });
    }
}

module.exports = DiaryEntryProcessor;
