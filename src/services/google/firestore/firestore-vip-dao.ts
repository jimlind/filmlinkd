export default class FirestoreVipDao {
    /** @type {FirebaseFirestore.CollectionReference} */
    configCollection;

    /**
     * @param {import("convict").Config} config
     * @param {import('@google-cloud/firestore')} firestoreLibrary
     */
    constructor(readonly config: any, readonly firestoreLibrary: any) {
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
            documentReference.get().then((documentSnapshot: any) => {
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
    parse(documentSnapshot: any) {
        return Object.values(documentSnapshot.data());
    }
}
