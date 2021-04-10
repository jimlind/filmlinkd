"use strict";

class DiaryEntryWriter {
    constructor(
        letterboxdDiary,
        messageEmbedFactory,
        discordMessageSender,
        firestoreSubscriptionDao,
        firestorePreviousDao,
        firestoreUserDao,
        logger
    ) {
        this.letterboxdDiary = letterboxdDiary;
        this.messageEmbedFactory = messageEmbedFactory;
        this.discordMessageSender = discordMessageSender;
        this.firestoreSubscriptionDao = firestoreSubscriptionDao;
        this.firestorePreviousDao = firestorePreviousDao;
        this.firestoreUserDao = firestoreUserDao;
        this.logger = logger;
    }

    postMostRecentEntry(userData, channelId) {
        this.letterboxdDiary.get(userData.userName, 1).then((diaryEntryList) => {
            if (diaryEntryList.length === 0) {
                return;
            }

            this.sendOneDiaryEntry(userData, diaryEntryList[0], channelId);
        });
    }

    postAllEntries() {
        this.firestoreSubscriptionDao.getActiveSubscriptions().then((documentList) => {
            this.logger.info(`Starting work on complete list of ${documentList.length} records.`);
            documentList.forEach((document, documentIndex) => {
                setTimeout(() => {
                    this.postAllEntriesForUser(document);
                }, documentIndex * 2000); // 2 second wait for each user
            });
        });
    }

    postPageOfEntries(rssDelay, pageSize) {
        return new Promise((resolve) => {
            this.firestoreSubscriptionDao.getActiveSubscriptionsPage(pageSize).then((documentList) => {
                const used = process.memoryUsage().heapUsed / 1024 / 1024;

                this.logger.info(`Starting work on page of ${documentList.length} records. [${used} MB]`);
                documentList.forEach((document, documentIndex) => {
                    setTimeout(() => {
                        // Update the document for the user
                        this.firestoreUserDao.resetChecked(document.id);

                        // Post any entries available for the user
                        this.postAllEntriesForUser(document);

                        // On the last document, resolve after waiting another second
                        if (documentList.length === documentIndex + 1) {
                            setTimeout(() => {
                                resolve();
                            }, rssDelay);
                        }
                    }, documentIndex * rssDelay); // Wait a delay for each user
                });
            });
        });
    }

    postAllEntriesForUser = function (userData) {
        this.getAllEntriesForUser(userData, 10).then((diaryEntryList) => {
            diaryEntryList.forEach((diaryEntry) => {
                userData.channelList.forEach((channel, channelIndex) => {
                    setTimeout(() => {
                        this.sendOneDiaryEntry(userData, diaryEntry, channel.channelId);
                    }, channelIndex * 1000); // 1 second wait for each channel
                });
            });
        });
    };

    getAllEntriesForUser = function (userData, maxDiaryEntries) {
        return new Promise((resolve) => {
            this.letterboxdDiary
                .get(userData.userName, maxDiaryEntries)
                .then((diaryEntryList) => {
                    const filteredDiaryEntryList = diaryEntryList.filter((diaryEntry) => {
                        // Exclude any entries older than the previous published
                        if (diaryEntry.publishedDate < userData.previous.published) {
                            return false;
                        }
                        // Exclude any entries that match the previous published link
                        if (diaryEntry.link === userData.previous.uri) {
                            return false;
                        }
                        // Otherwise include the entry
                        return true;
                    });
                    resolve(filteredDiaryEntryList);
                })
                .catch(() => {
                    const metadata = { userData };
                    this.logger.warn(`Could not fetch feed for "${userData.userName}" on Letterboxd`, { metadata });
                    resolve([]);
                });
        });
    };

    sendOneDiaryEntry(userData, diaryEntry, channelId) {
        const message = this.messageEmbedFactory.createDiaryEntryMessage(diaryEntry, userData);
        this.discordMessageSender
            .send(channelId, message)
            .then(() => {
                // Message send successful so update previous
                this.firestorePreviousDao.update(userData, diaryEntry);
            })
            .catch(() => {
                // Do Nothing. Send failure caught and logged in MessageSender
            });
    }
}

module.exports = DiaryEntryWriter;
