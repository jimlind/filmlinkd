export default class FirestoreUserDao {
    firestoreCollection;

    /**
     * @param {import('./firestore-connection.mjs')} firestoreConnection
     * @param {import('../../logger.mjs')} logger
     */
    constructor(firestoreConnection, logger) {
        this.firestoreCollection = firestoreConnection.getCollection();
        this.logger = logger;
    }

    /**
     * @param {string} letterboxdId
     * @param {string} userName
     * @param {string} displayName
     * @param {string} image
     * @returns {Promise<any>}
     */
    create(letterboxdId, userName, displayName, image) {
        const documentData = {
            letterboxdId,
            userName,
            displayName,
            image,
            created: Date.now(),
            checked: Date.now() + 60 * 60 * 1000, // Set checked an hour in the future to avoid edge cases
        };
        return new Promise((resolve) => {
            const documentReference = this.firestoreCollection.doc(userName);
            documentReference.set(documentData).then(() => {
                resolve(documentData);
            });
        });
    }

    /**
     * @param {string} userName
     * @returns {Promise<any>}
     * @deprecated
     */
    read(userName) {
        return new Promise((resolve, reject) => {
            const documentReference = this.firestoreCollection.doc(userName);
            documentReference.get().then((documentSnapshot) => {
                if (documentSnapshot.exists) {
                    resolve(documentSnapshot.data());
                } else {
                    reject();
                }
            });
        });
    }

    /**
     * @returns {Promise<number>}
     */
    count() {
        return this.firestoreCollection
            .count()
            .get()
            .then((countSnapshot) => {
                return countSnapshot.data().count;
            });
    }

    /**
     * While not deprecated you shouldn't rely on this method as a unique identifier
     * as users can and will change thier user name regularly on Letterboxd
     *
     * @param {string} userName
     * @returns {Promise<Object>}
     */
    getByUserName(userName) {
        const query = this.firestoreCollection.where('userName', '==', userName).limit(1);
        // Don't catch errors from this promise as they an expected failure from being called
        return query.get().then((querySnapshot) => {
            const documentSnapshotList = querySnapshot.docs;
            if (documentSnapshotList.length !== 1) {
                throw `User "${userName}" Does Not Exist`;
            }

            return documentSnapshotList[0]?.data() || null;
        });
    }

    /**
     * @param {string} letterboxdId
     * @returns {Promise<Object>}
     */
    getByLetterboxdId(letterboxdId) {
        const query = this.firestoreCollection.where('letterboxdId', '==', letterboxdId).limit(1);
        return query
            .get()
            .then((querySnapshot) => querySnapshot?.docs?.[0]?.data() || null)
            .catch(() => null);
    }

    /**
     * @param {string} userName
     * @param {string} displayName
     * @param {string} image
     * @returns {Promise<{ userName: string; displayName: string, image: string}>}}
     */
    update(userName, displayName, image) {
        const getDocumentSnapshot = this.firestoreCollection
            .where('userName', '==', userName)
            .limit(1)
            .get()
            .then((querySnapshot) => {
                const documentSnapshot = querySnapshot?.docs?.[0];
                if (!documentSnapshot) {
                    this.logger.warn('Unable to Update: User Not Found', userData);
                    throw 'Update Failed';
                }
                return documentSnapshot;
            });

        return getDocumentSnapshot
            .then((documentSnapshot) => {
                const userData = documentSnapshot.data();
                userData.displayName = displayName;
                userData.image = image;
                userData.updated = Date.now();
                return documentSnapshot.ref.update(userData);
            })
            .then(() => ({ userName, displayName, image }));
    }

    /**
     * Updates database record and returns updated record
     *
     * @param {string} letterboxdId
     * @param {string} userName
     * @param {string} displayName
     * @param {string} image
     * @returns {Promise<Object>}
     */
    updateByLetterboxdId(letterboxdId, userName, displayName, image) {
        const getDocumentSnapshot = this.firestoreCollection
            .where('letterboxdId', '==', letterboxdId)
            .limit(1)
            .get()
            .then((querySnapshot) => querySnapshot?.docs?.[0]);

        return getDocumentSnapshot
            .then((documentSnapshot) => {
                const data = { userName, displayName, image, updated: Date.now() };
                return documentSnapshot.ref.update(data);
            })
            .then(() => this.getByLetterboxdId(letterboxdId));
    }
}
