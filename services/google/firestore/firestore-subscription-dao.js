'use strict';

const User = require('../../../models/user');

class FirestoreSubscriptionDao {
    firestoreCollection;

    constructor(firestoreConnection, logger) {
        this.firestoreCollection = firestoreConnection.getCollection();
        this.logger = logger;
    }

    /**
     * @param {import('../../../models/user')} userData
     * @param {string} channelId
     * @param {string} guildId
     * @returns {Promise<{userData: import('../../../models/user'), success: boolean}>}
     */
    subscribe(userData, channelId, guildId) {
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
                channelList: channelList.concat([{ channelId, guildId }]),
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

    // Requiring guildID makes this a super easy query
    list(channelId, guildId) {
        const channel = { channelId: channelId, guildId: guildId };
        const query = this.firestoreCollection.where('channelList', 'array-contains', channel);

        return new Promise((resolve) => {
            query
                .get()
                .then((querySnapshot) => {
                    const userList = querySnapshot.docs.reduce(
                        (acc, current) => [...acc, current.id],
                        [],
                    );
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
     * @param {*} query
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
