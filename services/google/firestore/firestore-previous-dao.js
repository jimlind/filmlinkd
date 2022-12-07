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

        this.firestoreCollection
            .where('letterboxdId', '==', userData.letterboxdId)
            .get()
            .then((querySnapshot) => {
                if (!querySnapshot.docs.length) {
                    this.logger.warn('Unable to Update Previous: User Not Found', userData);
                    return;
                }

                querySnapshot.docs.forEach((documentSnapshot) => {
                    const userData = documentSnapshot.data();
                    userData.updated = Date.now();
                    userData.previous = {
                        id: diaryEntry.id,
                        lid: diaryEntry.lid,
                        published: diaryEntry.publishedDate,
                        uri: diaryEntry.link,
                    };
                    documentSnapshot.ref.update(userData).catch(() => {
                        this.logger.warn('Unable to Update Previous: Update Failed', userData);
                    });
                });
            });
    }
}

module.exports = FirestorePreviousDao;
