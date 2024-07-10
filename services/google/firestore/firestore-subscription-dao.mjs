export default class FirestoreSubscriptionDao {
    /** @type FirebaseFirestore.CollectionReference */
    firestoreCollection;

    /**
     * @param {import('./firestore-connection.mjs')} firestoreConnection
     * @param {import('./firestore-vip-dao.mjs')} firestoreVipDao
     * @param {import('../../logger.mjs')} logger
     * @param {import('../../../factories/user-factory.mjs')} userFactory
     */
    constructor(firestoreConnection, firestoreVipDao, logger, userFactory) {
        this.firestoreCollection = firestoreConnection.getCollection();
        this.firestoreVipDao = firestoreVipDao;
        this.logger = logger;
        this.userFactory = userFactory;
    }

    /**
     * @param {object} userData
     * @param {string} channelId
     * @returns {Promise<FirebaseFirestore.WriteResult>}>}
     */
    subscribe(userData, channelId) {
        // If the channel is already subscribed exit early
        const channelList = userData.channelList || [];
        if (channelList.some((channel) => channel.channelId === channelId)) {
            return Promise.all([]);
        }

        return this.firestoreCollection
            .where('letterboxdId', '==', userData.letterboxdId)
            .get()
            .then((querySnapshot) => querySnapshot?.docs?.[0])
            .then((documentSnapshot) =>
                documentSnapshot.ref.update({
                    channelList: channelList.concat([{ channelId }]),
                    updated: Date.now(),
                }),
            )
            .catch(() => {
                // If subscription failed for the user log the warning but largely ignore it
                const metadata = { userData, channelId };
                this.logger.warn('Unable to Subscribe', metadata);
                return Promise.all([]);
            });
    }

    /**
     * @param {object} userData
     * @param {string} channelId
     * @returns {Promise<FirebaseFirestore.WriteResult>}>}
     */
    unsubscribe(userData, channelId) {
        const channelList = userData.channelList || [];
        const newChannelList = channelList.filter((channel) => {
            return channel.channelId !== channelId;
        });

        // If the channel is not subscribed (nothing was filtered) exit early
        if (channelList.length === newChannelList.length) {
            return Promise.all([]);
        }

        return this.firestoreCollection
            .where('letterboxdId', '==', userData.letterboxdId)
            .get()
            .then((querySnapshot) => querySnapshot?.docs?.[0])
            .then((documentSnapshot) =>
                documentSnapshot.ref.update({
                    channelList: newChannelList,
                    updated: Date.now(),
                }),
            )
            .catch(() => {
                // If unsubscribe failed for the user log the warning but largely ignore it
                const metadata = { userData, channelId };
                this.logger.warn('Unable to Unsubscribe', metadata);
                return Promise.all([]);
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
                        channelData.map((i) => ({ channelId: i.channelId })),
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
                        const user = Object.assign(this.userFactory.create(), documentData);
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
