export default class FirestoreConfigDao {
    configCollection;

    /**
     * @param {import('convict').Config} config
     * @param {import('@google-cloud/firestore')} firestoreLibrary
     */
    constructor(readonly config: any, readonly firestoreLibrary: any) {
        const database = new firestoreLibrary.Firestore({
            projectId: this.config.get('googleCloudProjectId'),
            keyFilename: this.config.get('googleCloudIdentityKeyFile'),
        });
        this.configCollection = database.collection('config');
    }

    read() {
        const configId = this.config.isDev ? 'development' : 'production';
        return new Promise((resolve, reject) => {
            const documentReference = this.configCollection.doc(configId);
            documentReference.get().then((documentSnapshot: any) => {
                if (documentSnapshot.exists) {
                    resolve(documentSnapshot.data());
                } else {
                    reject();
                }
            });
        });
    }
}
