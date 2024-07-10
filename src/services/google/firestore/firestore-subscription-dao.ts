export default class FirestoreSubscriptionDao {
    /** @type FirebaseFirestore.CollectionReference */
    firestoreCollection;

    /**
     * @param {import('./firestore-connection.mjs')} firestoreConnection
     * @param {import('./firestore-vip-dao.mjs')} firestoreVipDao
     * @param {import('../../logger.mjs')} logger
     * @param {import('../../../factories/user-factory.mjs')} userFactory
     */
    constructor(
        readonly firestoreConnection: any,
        readonly firestoreVipDao: any,
        readonly logger: any,
        readonly userFactory: any,
    ) {
        this.firestoreCollection = firestoreConnection.getCollection();
    }

    /**
     * @param {object} userData
     * @param {string} channelId
     * @returns {Promise<FirebaseFirestore.WriteResult>}>}
     */
    subscribe(userData: { channelList: any; letterboxdId: any }, channelId: any) {
        // If the channel is already subscribed exit early
        const channelList = userData.channelList || [];
        if (channelList.some((channel: { channelId: any }) => channel.channelId === channelId)) {
            return Promise.all([]);
        }

        return this.firestoreCollection
            .where('letterboxdId', '==', userData.letterboxdId)
            .get()
            .then((querySnapshot: { docs: any[] }) => querySnapshot?.docs?.[0])
            .then(
                (documentSnapshot: {
                    ref: { update: (arg0: { channelList: any; updated: number }) => any };
                }) =>
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
    unsubscribe(userData: { channelList: never[]; letterboxdId: any }, channelId: any) {
        const channelList = userData.channelList || [];
        const newChannelList = channelList.filter((channel: { channelId: any }) => {
            return channel.channelId !== channelId;
        });

        // If the channel is not subscribed (nothing was filtered) exit early
        if (channelList.length === newChannelList.length) {
            return Promise.all([]);
        }

        return this.firestoreCollection
            .where('letterboxdId', '==', userData.letterboxdId)
            .get()
            .then((querySnapshot: { docs: any[] }) => querySnapshot?.docs?.[0])
            .then(
                (documentSnapshot: {
                    ref: { update: (arg0: { channelList: any; updated: number }) => any };
                }) =>
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
    list(channelId: any) {
        const channel = { channelId: channelId };
        const query = this.firestoreCollection.where('channelList', 'array-contains', channel);

        return new Promise((resolve) => {
            query
                .get()
                .then((querySnapshot: { docs: any[] }) => {
                    const userList = querySnapshot.docs.reduce(
                        (accumulator: any, current: { get: (arg0: string) => any }) => {
                            const data = {
                                userName: current.get('userName'),
                                lid: current.get('letterboxdId'),
                            };
                            return [...accumulator, data];
                        },
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
     * @returns {Promise<User[]>}
     */
    getVipSubscriptions() {
        return new Promise((resolve, reject) => {
            this.firestoreVipDao
                .read()
                .then((channelData: any[]) => {
                    const query = this.firestoreCollection.where(
                        'channelList',
                        'array-contains-any',
                        channelData.map((i: { channelId: any }) => ({ channelId: i.channelId })),
                    );
                    return this.getUserListFromQuery(query);
                })
                .then((userList: unknown) => {
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
    getUserListFromQuery(query: { get: () => Promise<any> }) {
        const userList: any = [];

        return new Promise((resolve) => {
            query
                .get()
                .then((querySnapshot: any[]) => {
                    querySnapshot.forEach(
                        (documentSnapshot: { ref: { id: any }; data: () => any }) => {
                            const documentData = {
                                id: documentSnapshot.ref.id,
                                ...documentSnapshot.data(),
                            };
                            const user = Object.assign(this.userFactory.create(), documentData);
                            userList.push(user); // TODO: Do we need the complete data blob or can we trim it?
                        },
                    );
                    resolve(userList);
                })
                .catch(() => {
                    // If any error resolve as an empty user list
                    resolve([]);
                });
        });
    }
}
