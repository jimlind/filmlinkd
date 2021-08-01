'use strict';

const { Firestore } = require('@google-cloud/firestore');

class FirestoreVipDao {
    /** @type {import("../../../models/config")} */
    config;
    /** @type {FirebaseFirestore.CollectionReference} */
    configCollection;

    /**
     * @param {import("../../../models/config")} config
     */
    constructor(config) {
        this.config = config;
        const database = new Firestore({
            projectId: this.config.googleCloudProjectId,
            keyFilename: this.config.gcpKeyFile,
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

module.exports = FirestoreVipDao;
