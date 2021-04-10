'use strict';

const {Firestore} = require('@google-cloud/firestore');

class FirestoreConnection {
    collection;

    constructor(config) {
        this.config = config;

        const database = new Firestore({
            projectId: this.config.googleCloudProjectId,
            keyFilename: this.config.gcpKeyFile,
        });
        this.collection = database.collection(this.config.firestoreCollectionId);
    }

    getCollection() {
        return this.collection;
    }
}

module.exports = FirestoreConnection;