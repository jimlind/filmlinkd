'use strict';

class DiaryEntryProcessor {
    /**
     * @param {import('../diary-entry/diary-entry-publisher')} diaryEntryPublisher
     * @param {import('../letterboxd/letterboxd-diary-rss')} letterboxdDiaryRss
     * @param {import('../letterboxd/letterboxd-likes-web')} letterboxdLikesWeb
     * @param {import('../logger')} logger
     * @param {import('../subscribed-user-list')} subscribedUserList
     */
    constructor(
        diaryEntryPublisher,
        letterboxdDiaryRss,
        letterboxdLikesWeb,
        logger,
        subscribedUserList,
    ) {
        this.diaryEntryPublisher = diaryEntryPublisher;
        this.letterboxdDiaryRss = letterboxdDiaryRss;
        this.letterboxdLikesWeb = letterboxdLikesWeb;
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
            this.diaryEntryPublisher.publish(diaryEntryList, [channelId]);
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
                    // Special usecase for empty user list returned.
                    if (!userList.length) {
                        return resolve(0);
                    }
                    // Loop over the users and with a slight delay between each request
                    let userListPostCount = 0;
                    userList.forEach((user, userIndex) => {
                        setTimeout(() => {
                            this.getNewEntriesForUser(user, 10).then((diaryEntryList) => {
                                if (diaryEntryList.length) {
                                    const message = `Publishing "${diaryEntryList[0].filmTitle}" from VIP "${diaryEntryList[0].userName}"`;
                                    this.logger.info(message);
                                }
                                this.diaryEntryPublisher
                                    .publish(diaryEntryList)
                                    .then((successList) => {
                                        successList.forEach((success) => {
                                            this.subscribedUserList.upsert(
                                                success.user,
                                                parseInt(success.id),
                                                true,
                                            );
                                        });
                                    });
                            });
                            userListPostCount++;
                            if (userList.length === userListPostCount) {
                                return resolve(userListPostCount);
                            }
                        }, userIndex * 10);
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
}

module.exports = DiaryEntryProcessor;
