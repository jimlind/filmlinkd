'use strict';

const User = require('../../../models/user');

class FirestoreSubscriptionDao {
    /** @type FirebaseFirestore.CollectionReference */
    firestoreCollection;

    /**
     * @param {import('./firestore-connection')} firestoreConnection
     * @param {import('./firestore-vip-dao')} firestoreVipDao
     * @param {import('../../logger.mjs')} logger
     */
    constructor(firestoreConnection, firestoreVipDao, logger) {
        this.firestoreCollection = firestoreConnection.getCollection();
        this.firestoreVipDao = firestoreVipDao;
        this.logger = logger;
    }

    /**
     * @param {object} userData
     * @param {string} channelId
     * @returns {Promise<{userData: object, success: boolean}>}
     */
    subscribe(userData, channelId) {
        return new Promise((resolve, reject) => {
            const channelList = userData.channelList || [];

            // If the channel is already subscribed reject
            if (channelList.some((channel) => channel.channelId === channelId)) {
                resolve({ userData, success: false });
                return;
            }

            // Create new data object with new channels and timestamp
            const updatedUserData = {
                ...userData,
                channelList: channelList.concat([{ channelId }]),
                updated: Date.now(),
            };

            const documentReference = this.firestoreCollection.doc(updatedUserData.userName);
            documentReference
                .update(updatedUserData)
                .then(() => {
                    resolve({ userData: updatedUserData, success: true });
                })
                .catch(() => {
                    const metadata = {
                        channelId,
                        updatedUserData,
                    };
                    this.logger.warn('Unable to Subscribe', metadata);
                    reject();
                });
        });
    }

    /**
     * @param {string} userName
     * @param {string} channelId
     */
    unsubscribe(userName, channelId) {
        return new Promise((resolve, reject) => {
            const documentReference = this.firestoreCollection.doc(userName);
            documentReference.get().then((documentSnapshot) => {
                if (!documentSnapshot.exists) {
                    reject();
                    return;
                }

                const documentData = documentSnapshot.data();
                if (!documentData.channelList.some((channel) => channel.channelId === channelId)) {
                    reject();
                    return;
                }

                documentData.channelList = documentData.channelList.filter((channel) => {
                    return channel.channelId !== channelId;
                });
                documentReference
                    .update(documentData)
                    .then(() => {
                        resolve(documentData);
                    })
                    .catch(() => {
                        const metadata = {
                            channelId,
                            documentData,
                        };
                        this.logger.warn('Unable to Unsubscribe', metadata);
                    });
            });
        });
    }

    /**
     * @param {string} channelId
     * @returns {Promise<string[]>}
     */
    list(channelId) {
        const channel = { channelId: channelId };
        const query = this.firestoreCollection.where('channelList', 'array-contains', channel);

        return new Promise((resolve) => {
            query
                .get()
                .then((querySnapshot) => {
                    const userList = querySnapshot.docs.reduce((accumulator, current) => {
                        const data = {
                            userName: current.get('userName'),
                            lid: current.get('letterboxdId'),
                        };
                        return [...accumulator, data];
                    }, []);
                    resolve(userList);
                })
                .catch(() => {
                    resolve([]);
                });
        });
    }

    /**
     * @returns {Promise<User[]>}
     */
    getActiveSubscriptions() {
        const query = this.firestoreCollection.where('channelList', '!=', []);

        return this.getUserListFromQuery(query);
    }

    /**
     * @returns {Promise<User[]>}
     */
    getVipSubscriptions() {
        return new Promise((resolve, reject) => {
            this.firestoreVipDao
                .read()
                .then((channelData) => {
                    const query = this.firestoreCollection.where(
                        'channelList',
                        'array-contains-any',
                        channelData,
                    );
                    return this.getUserListFromQuery(query);
                })
                .then((userList) => {
                    resolve(userList);
                })
                .catch(() => {
                    reject('Error getting VIP subscribers');
                });
        });
    }

    /**
     * @param {FirebaseFirestore.Query} query
     * @returns {Promise<User[]>}
     */
    getUserListFromQuery(query) {
        const userList = [];

        return new Promise((resolve) => {
            query
                .get()
                .then((querySnapshot) => {
                    querySnapshot.forEach((documentSnapshot) => {
                        const documentData = {
                            id: documentSnapshot.ref.id,
                            ...documentSnapshot.data(),
                        };
                        const user = Object.assign(new User(), documentData);
                        userList.push(user); // TODO: Do we need the complete data blob or can we trim it?
                    });
                    resolve(userList);
                })
                .catch(() => {
                    // If any error resolve as an empty user list
                    resolve([]);
                });
        });
    }
}

module.exports = FirestoreSubscriptionDao;
