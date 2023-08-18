export default class FirestorePreviousDao {
    /**
     * @param {import('../../../models/config')} config
     * @param {import('../firestore/firestore-connection')} firestoreConnection
     * @param {import {'../letterboxd/letterboxd-lid-comparison'}} letterboxdLidComparison
     * @param {import('../../logger')} logger
     */
    constructor(config, firestoreConnection, letterboxdLidComparison, logger) {
        this.config = config;
        this.firestoreCollection = firestoreConnection.getCollection();
        this.letterboxdLidComparison = letterboxdLidComparison;
        this.logger = logger;
    }

    /**
     * @param {import('../../../models/user')} userData
     * @param {import('../../../models/diary-entry')} diaryEntry
     * @returns void
     */
    update(userData, diaryEntry) {
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
                    userData.previous = userData?.previous || {};

                    // Update all other previous assets if the diary entry is the newest
                    const entryComparison = this.letterboxdLidComparison.compare(
                        userData?.previous?.lid || '0',
                        diaryEntry.lid,
                    );
                    if (entryComparison === 1) {
                        userData.previous.id = diaryEntry.id;
                        userData.previous.lid = diaryEntry.lid;
                        userData.previous.published = diaryEntry.publishedDate;
                        userData.previous.uri = diaryEntry.link;
                        userData.previous.list = this.preparePreviousList(
                            userData?.previous?.list || [],
                            diaryEntry.lid,
                        );
                    }

                    documentSnapshot.ref.update(userData).catch(() => {
                        this.logger.warn('Unable to Update Previous: Update Failed', userData);
                    });
                });
            });
    }

    preparePreviousList(existingList, newItem) {
        const newList = [...new Set([...existingList, newItem])];
        newList.sort((a, b) => this.letterboxdLidComparison.compare(b, a));
        return newList.slice(-10);
    }
}
