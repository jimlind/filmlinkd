const { Firestore } = require('@google-cloud/firestore');

class FirestoreConfigDao {
    configCollection;

    constructor(config) {
        this.config = config;
        const database = new Firestore({
            projectId: this.config.get('googleCloudProjectId'),
            keyFilename: this.config.get('googleCloudIdentityKeyFile'),
        });
        this.configCollection = database.collection('config');
    }

    read() {
        const configId = this.config.isDev ? 'development' : 'production';
        return new Promise((resolve, reject) => {
            const documentReference = this.configCollection.doc(configId);
            documentReference.get().then((documentSnapshot) => {
                if (documentSnapshot.exists) {
                    resolve(documentSnapshot.data());
                } else {
                    reject();
                }
            });
        });
    }
}

module.exports = FirestoreConfigDao;
