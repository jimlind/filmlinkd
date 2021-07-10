'use strict';

const User = require('../../../models/user');

class FirestoreSubscriptionDao {
    firestoreCollection;

    constructor(firestoreConnection, logger) {
        this.firestoreCollection = firestoreConnection.getCollection();
        this.logger = logger;
    }

    subscribe(data, channelId, guildId) {
        return new Promise((resolve, reject) => {
            const channelList = data.channelList || [];

            // If the channel is already subscribed reject
            if (channelList.some((channel) => channel.channelId === channelId)) {
                reject();
                return;
            }

            // Create new data object with new channels and timestamp
            const updatedData = {
                ...data,
                channelList: channelList.concat([{ channelId, guildId }]),
                updated: Date.now(),
            };

            const documentReference = this.firestoreCollection.doc(data.userName);
            documentReference
                .update(updatedData)
                .then(() => {
                    resolve(updatedData);
                })
                .catch((error) => {
                    const metadata = {
                        channelId,
                        userData,
                    };
                    this.logger.warn('Unable to Subscribe', metadata);
                });
        });
    }

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
                    .catch((error) => {
                        const metadata = {
                            channelId,
                            userData,
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
