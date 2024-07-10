export default class FirestoreConnection {
    collection;

    /**
     * @param {import('convict').Config} config
     * @param {import('@google-cloud/firestore')} firestoreLibrary
     */
    constructor(config, firestoreLibrary) {
        this.config = config;

        const database = new firestoreLibrary.Firestore({
            projectId: this.config.get('googleCloudProjectId'),
            keyFilename: this.config.get('googleCloudIdentityKeyFile'),
        });
        this.collection = database.collection(this.config.get('firestoreCollectionId'));
    }

    getCollection() {
        return this.collection;
    }
}
