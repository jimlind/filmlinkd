export default class FirestoreVipDao {
    /** @type {import("convict").Config} */
    config;
    /** @type {FirebaseFirestore.CollectionReference} */
    configCollection;

    /**
     * @param {import("convict").Config} config
     * @param {import('@google-cloud/firestore')} firestoreLibrary
     */
    constructor(config, firestoreLibrary) {
        this.config = config;
        const database = new firestoreLibrary.Firestore({
            projectId: this.config.get('googleCloudProjectId'),
            keyFilename: this.config.get('googleCloudIdentityKeyFile'),
        });
        this.configCollection = database.collection('vip');
    }

    /**
     * @returns Promise<{ channelId: number; guildId: number; }[]>
     */
    read() {
        return new Promise((resolve, reject) => {
            const documentReference = this.configCollection.doc('channels');
            documentReference.get().then((documentSnapshot) => {
                if (documentSnapshot.exists) {
                    resolve(this.parse(documentSnapshot));
                } else {
                    reject();
                }
            });
        });
    }

    /**
     * @param {FirebaseFirestore.DocumentSnapshot} documentSnapshot
     * @returns {{ channelId: number; guildId: number; }[]}
     */
    parse(documentSnapshot) {
        return Object.values(documentSnapshot.data());
    }
}
