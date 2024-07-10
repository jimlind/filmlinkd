export default class FirestoreUserDao {
    firestoreCollection;

    /**
     * @param {import('./firestore-connection.mjs')} firestoreConnection
     * @param {import('../../logger.mjs')} logger
     */
    constructor(readonly firestoreConnection: any, readonly logger: any) {
        this.firestoreCollection = firestoreConnection.getCollection();
    }

    /**
     * @param {string} letterboxdId
     * @param {string} userName
     * @param {string} displayName
     * @param {string} image
     * @returns {Promise<any>}
     */
    create(letterboxdId: any, userName: any, displayName: any, image: any) {
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
    read(userName: any) {
        return new Promise((resolve, reject) => {
            const documentReference = this.firestoreCollection.doc(userName);
            documentReference.get().then((documentSnapshot: any) => {
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
            .then((countSnapshot: any) => {
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
    getByUserName(userName: any) {
        const query = this.firestoreCollection.where('userName', '==', userName).limit(1);
        // Don't catch errors from this promise as they an expected failure from being called
        return query.get().then((querySnapshot: any) => {
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
    getByLetterboxdId(letterboxdId: any) {
        const query = this.firestoreCollection.where('letterboxdId', '==', letterboxdId).limit(1);
        return query
            .get()
            .then((querySnapshot: any) => querySnapshot?.docs?.[0]?.data() || null)
            .catch(() => null);
    }

    /**
     * @param {string} userName
     * @param {string} displayName
     * @param {string} image
     * @returns {Promise<{ userName: string; displayName: string, image: string}>}}
     */
    update(userName: any, displayName: any, image: any) {
        const getDocumentSnapshot = this.firestoreCollection
            .where('userName', '==', userName)
            .limit(1)
            .get()
            .then((querySnapshot: any) => {
                const documentSnapshot = querySnapshot?.docs?.[0];
                if (!documentSnapshot) {
                    this.logger.warn('Unable to Update: User Not Found', userName);
                    throw 'Update Failed';
                }
                return documentSnapshot;
            });

        return getDocumentSnapshot
            .then((documentSnapshot: any) => {
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
    updateByLetterboxdId(letterboxdId: any, userName: any, displayName: any, image: any) {
        const getDocumentSnapshot = this.firestoreCollection
            .where('letterboxdId', '==', letterboxdId)
            .limit(1)
            .get()
            .then((querySnapshot: any) => querySnapshot?.docs?.[0]);

        return getDocumentSnapshot
            .then((documentSnapshot: any) => {
                const data = { userName, displayName, image, updated: Date.now() };
                return documentSnapshot.ref.update(data);
            })
            .then(() => this.getByLetterboxdId(letterboxdId));
    }
}
