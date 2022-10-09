'use strict';

class FirestorePreviousDao {
    /**
     * @param {import('../../../models/config')} config
     * @param {import('../firestore/firestore-connection')} firestoreConnection
     * @param {import('../../logger')} logger
     */
    constructor(config, firestoreConnection, logger) {
        this.config = config;
        this.firestoreCollection = firestoreConnection.getCollection();
        this.logger = logger;
    }

    /**
     * @param {import('../../../models/user')} userData
     * @param {import('../../../models/diary-entry')} diaryEntry
     * @returns void
     */
    update(userData, diaryEntry) {
        // If the id for previous is later than the new entry, skip it.
        if (userData?.previous?.id >= diaryEntry.id) {
            return;
        }

        // Set previous and override anything that may have previously existed
        userData.previous = {
            id: diaryEntry.id,
            lid: diaryEntry.lid,
            published: diaryEntry.publishedDate,
            uri: diaryEntry.link,
        };
        userData.updated = Date.now();

        // Update the database record
        const documentReference = this.firestoreCollection.doc(userData.userName);
        documentReference.update(userData).catch(() => {
            const metadata = {
                userData,
                diaryEntry,
            };
            this.logger.warn('Unable to Update Previous', metadata);
        });
    }
}

module.exports = FirestorePreviousDao;
