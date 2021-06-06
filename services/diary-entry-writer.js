'use strict';

class DiaryEntryWriter {
    constructor(
        letterboxdDiaryRss,
        letterboxdLikesWeb,
        messageEmbedFactory,
        discordMessageSender,
        firestoreSubscriptionDao,
        firestorePreviousDao,
        firestoreUserDao,
        logger,
        subscribedUserList,
    ) {
        this.letterboxdDiaryRss = letterboxdDiaryRss;
        this.letterboxdLikesWeb = letterboxdLikesWeb;
        this.messageEmbedFactory = messageEmbedFactory;
        this.discordMessageSender = discordMessageSender;
        this.firestoreSubscriptionDao = firestoreSubscriptionDao;
        this.firestorePreviousDao = firestorePreviousDao;
        this.firestoreUserDao = firestoreUserDao;
        this.logger = logger;
        this.subscribedUserList = subscribedUserList;
    }

    postMostRecentEntry(userData, channelId) {
        this.letterboxdDiaryRss.get(userData.userName, 1).then((diaryEntryList) => {
            if (diaryEntryList.length === 0) {
                return;
            }
            const diaryEntry = diaryEntryList[0];
            this.letterboxdLikesWeb
                .get(userData.userName, [diaryEntry.link])
                .then((likedFilmList) => {
                    diaryEntry.liked = likedFilmList.length
                        ? likedFilmList.includes(diaryEntry.link)
                        : false;
                })
                .catch(() => {
                    this.logger.warn(
                        `Could not fetch film likes for "${userData.userName}" on Letterboxd`,
                    );
                })
                .finally(() => {
                    this.sendOneDiaryEntry(userData, diaryEntry, channelId);
                });
        });
    }

    postPageOfEntries(index, pageSize) {
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
                    // Post any entries available for the user
                    this.postEntriesForUser(user, 10).then(() => {
                        userListPostCount++;
                        if (userList.length === userListPostCount) {
                            return resolve(userListPostCount);
                        }
                    });
                });
            });
        });
    }

    postEntriesForUser = function (user, maxDiaryEntries) {
        return new Promise((resolve) => {
            this.getEntriesForUser(user.userName, user.previousId, maxDiaryEntries)
                .then((diaryEntryList) => {
                    if (diaryEntryList.length === 0) {
                        return resolve(); // Exit early if no diary entries
                    }

                    this.firestoreUserDao
                        .read(user.userName)
                        .then((userData) => {
                            if (userData.channelList === 0) {
                                return resolve(); // Exit early if no subscribed channels
                            }

                            diaryEntryList.forEach((diaryEntry, diaryEntryIndex) => {
                                setTimeout(() => {
                                    userData.channelList.forEach((channel, channelIndex) => {
                                        const channelId = channel.channelId;
                                        this.sendOneDiaryEntry(userData, diaryEntry, channelId)
                                            .then(() => {
                                                if (
                                                    diaryEntryIndex == diaryEntryList.length - 1 &&
                                                    channelIndex == userData.channelList.length - 1
                                                ) {
                                                    // Sent all messages on all channels
                                                    return resolve();
                                                }
                                            })
                                            .catch(() => {
                                                // Unable to send diary entry
                                                return resolve();
                                            });
                                    });
                                }, diaryEntryIndex * 1000); // Wait 1 second for each diary entry
                            });
                        })
                        .catch(() => {
                            // Unable to read user data from DAO
                            return resolve();
                        });
                })
                .catch(() => {
                    // Unable to read diary entries from Letterboxd
                    return resolve();
                });
        });
    };

    getEntriesForUser = function (userName, previousId, maxDiaryEntries) {
        return new Promise((resolve) => {
            this.letterboxdDiaryRss
                .get(userName, maxDiaryEntries)
                .then((diaryEntryList) => {
                    let filteredDiaryEntryList = diaryEntryList.filter((diaryEntry) => {
                        // Include any entry newer than last logged
                        return diaryEntry.id > previousId;
                    });
                    const linkList = diaryEntryList.map((diaryEntry) => diaryEntry.link);

                    this.letterboxdLikesWeb
                        .get(userName, linkList)
                        .then((likedFilmLinkList) => {
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
                                `Could not fetch film likes for "${userName}" on Letterboxd`,
                            );
                        })
                        .finally(() => {
                            return resolve(filteredDiaryEntryList);
                        });
                })
                .catch(() => {
                    this.logger.warn(`Could not fetch feed for "${userName}" on Letterboxd`);
                    return resolve([]);
                });
        });
    };

    sendOneDiaryEntry(userData, diaryEntry, channelId) {
        return new Promise((resolve) => {
            this.discordMessageSender.getPermissions(channelId).then((permissions) => {
                const message = this.messageEmbedFactory.createDiaryEntryMessage(
                    diaryEntry,
                    userData,
                    permissions,
                );
                this.discordMessageSender
                    .send(channelId, message)
                    .then(() => {
                        // Message send successful so update previous
                        // TODO: Combine these two update methods
                        this.firestorePreviousDao.update(userData, diaryEntry);
                        this.subscribedUserList.upsert(userData.userName, diaryEntry.id);
                        // Work completed. Updates can be async.
                        return resolve();
                    })
                    .catch(() => {
                        // Do Nothing. Send failure caught and logged in MessageSender
                        return resolve();
                    });
            });
        });
    }
}

module.exports = DiaryEntryWriter;
