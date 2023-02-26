'use strict';

const { Firestore } = require('@google-cloud/firestore');

class FirestoreConnection {
    collection;

    /**
     * @param {import('convict').Config} config
     */
    constructor(config) {
        this.config = config;

        const database = new Firestore({
            projectId: this.config.get('googleCloudProjectId'),
            keyFilename: this.config.get('gcpKeyFile'),
        });
        this.collection = database.collection(this.config.get('firestoreCollectionId'));
    }

    getCollection() {
        return this.collection;
    }
}

module.exports = FirestoreConnection;
