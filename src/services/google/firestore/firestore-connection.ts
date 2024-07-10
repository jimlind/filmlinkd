export default class FirestoreConnection {
    collection;

    /**
     * @param {import('convict').Config} config
     * @param {import('@google-cloud/firestore')} firestoreLibrary
     */
    constructor(readonly config: any, readonly firestoreLibrary: any) {
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
