"use strict";

class FirestorePreviousDao {
    firestoreCollection;

    constructor(config, firestoreConnection, logger) {
        this.config = config;
        this.firestoreCollection = firestoreConnection.getCollection();
        this.logger = logger;
    }

    update(userData, diaryEntry) {
        // If the database record for previous is later than the new entry, skip it.
        if (userData?.previous?.published > diaryEntry.publishedDate) {
            return;
        }

        // Set previous and override anything that may have previously existed
        userData.previous = { id: diaryEntry.id, published: diaryEntry.publishedDate, uri: diaryEntry.link };
        userData.updated = Date.now();

        // Update the database record
        const documentReference = this.firestoreCollection.doc(userData.userName);
        documentReference.update(userData).catch(() => {
            const metadata = {
                userData,
                diaryEntry,
            };
            this.logger.warn("Unable to Update Previous", metadata);
        });
    }
}

module.exports = FirestorePreviousDao;
