'use strict';

class FirestoreUserDao {
    firestoreCollection;

    constructor(firestoreConnection) {
        this.firestoreCollection = firestoreConnection.getCollection();;
    }

    create(userName, displayName, image) {
        const documentData = {
            userName,
            displayName,
            image,
            created: Date.now(),
            checked: Date.now() + (60 * 60 * 1000), // Set checked an hour in the future to avoid edge cases
        };
        return new Promise((resolve) => {
            const documentReference = this.firestoreCollection.doc(userName);
            documentReference.set(documentData)
                .then(() => {
                    resolve(documentData);
                });
        });
    }

    read(userName) {
        return new Promise((resolve, reject) => {
            const documentReference = this.firestoreCollection.doc(userName);
            documentReference.get()
                .then((documentSnapshot) => {
                    if (documentSnapshot.exists) {
                        resolve(documentSnapshot.data());
                    } else {
                        reject();
                    }
                });
        })
    }

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
                    updated: Date.now()
                }
                documentReference.update(data).then(() => {
                    resolve(data);
                });
            });
        });
    }

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
                    checked: Date.now()
                }
                documentReference.update(data).then(() => {
                    resolve(data);
                });
            });
        });
    }
}

module.exports = FirestoreUserDao;