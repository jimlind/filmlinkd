'use strict';

class FirestoreUserDao {
    firestoreCollection;

    /**
     * @param {import('./firestore-connection')} firestoreConnection
     */
    constructor(firestoreConnection) {
        this.firestoreCollection = firestoreConnection.getCollection();
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
     * @param {string} userName
     * @returns {Promise<Object>}
     */
    getByUserName(userName) {
        const query = this.firestoreCollection.where('userName', '==', userName).limit(1);
        return query.get().then((querySnapshot) => {
            const documentSnapshotList = querySnapshot.docs;
            if (documentSnapshotList.length !== 1) {
                throw `User "${userName}" Does Not Exist`;
            }

            return documentSnapshotList[0]?.data() || null;
        });
    }

    /**
     * @param {string} userName
     * @param {string} displayName
     * @param {string} image
     * @returns {Promise<any>}
     */
    update(userName, displayName, image) {
        return new Promise((resolve, reject) => {
            const documentReference = this.firestoreCollection.doc(userName);
            documentReference.get().then((documentSnapshot) => {
                if (!documentSnapshot.exists) {
                    reject();
                    return;
                }

                const data = {
                    ...documentSnapshot.data(),
                    displayName,
                    image,
                    updated: Date.now(),
                };
                documentReference.update(data).then(() => {
                    resolve(data);
                });
            });
        });
    }

    /**
     * @param {string} userName
     * @returns {Promise<any>}
     */
    resetChecked(userName) {
        return new Promise((resolve, reject) => {
            const documentReference = this.firestoreCollection.doc(userName);
            documentReference.get().then((documentSnapshot) => {
                if (!documentSnapshot.exists) {
                    reject();
                    return;
                }

                const data = {
                    ...documentSnapshot.data(),
                    checked: Date.now(),
                };
                documentReference.update(data).then(() => {
                    resolve(data);
                });
            });
        });
    }
}

module.exports = FirestoreUserDao;
