export default class SubscribedUserList {
    /**
     * @type {[key: string]: string} | null}
     */
    cachedData = null;

    /**
     * @type {[key: string]: string} | null}
     */
    cachedVipData = null;

    /**
     * @param {import('./google/firestore/firestore-subscription-dao.mjs')} firestoreSubscriptionDao
     * @param {import('./letterboxd/letterboxd-lid-comparison.mjs')} letterboxdLidComparison
     * @param {import('./logger.mjs')} logger
     */
    constructor(firestoreSubscriptionDao, letterboxdLidComparison, logger) {
        this.firestoreSubscriptionDao = firestoreSubscriptionDao;
        this.letterboxdLidComparison = letterboxdLidComparison;
        this.logger = logger;
    }

    /**
     * @returns {Promise<{[key: string]: string>}}
     */
    getAllActiveSubscriptions() {
        const dao = this.firestoreSubscriptionDao;
        const method = dao.getActiveSubscriptions.bind(dao);
        return this.getActiveSubscriptionsBaseMethod(method, 'cachedData', 'users');
    }

    /**
     * @returns {Promise<{[key: string]: string>}}
     */
    getVipActiveSubscriptions() {
        const dao = this.firestoreSubscriptionDao;
        const method = dao.getVipSubscriptions.bind(dao);
        return this.getActiveSubscriptionsBaseMethod(method, 'cachedVipData', 'VIPs');
    }

    /**
     * @param {*} daoMethod
     * @param {string} cacheName
     * @param {string} userLabel
     * @returns {Promise<{[key: string]: string>}}
     */
    getActiveSubscriptionsBaseMethod(daoMethod, cacheName, userLabel) {
        if (this[cacheName]) {
            return new Promise((resolve) => resolve(this[cacheName]));
        }

        return daoMethod()
            .then((userList) => {
                const logData = { userCount: userList.length };
                this.logger.info(`Loaded and cached ${userLabel}`, logData);

                const reducer = (data, user) =>
                    Object.assign(data, { [user.letterboxdId]: user?.previous?.lid || '' });
                return userList.reduce(reducer, {});
            })
            .then((dataObject) => {
                this[cacheName] = dataObject;
                return dataObject;
            });
    }

    /**
     * @returns {Promise<{ number }>}}
     */
    getRandomIndex() {
        return new Promise((resolve) => {
            this.getAllActiveSubscriptions().then((subscriberList) => {
                return resolve(Math.floor(Math.random() * Object.values(subscriberList).length));
            });
        });
    }

    /**
     * @param {number} start The starting index assuming zero-indexed array
     * @param {number} pageSize The total number of entries returned
     * @returns {Promise<{ userLid: string; entryLid: string}[]>}}
     */
    getActiveSubscriptionsPage(start, pageSize) {
        return this.getActiveSubscriptionsPageBaseMethod(
            start,
            pageSize,
            this.getAllActiveSubscriptions,
        );
    }

    /**
     * @param {number} start The starting index assuming zero-indexed array
     * @param {number} pageSize The total number of entries returned
     * @returns {Promise<{[key: string]: {entryId: number; entryLid: string;}>}}
     */
    getActiveVipSubscriptionsPage(start, pageSize) {
        return this.getActiveSubscriptionsPageBaseMethod(
            start,
            pageSize,
            this.getVipActiveSubscriptions,
        );
    }

    /**
     * @param {number} start The starting index assuming zero-indexed array
     * @param {number} pageSize The total number of entries returned
     * @param {*} userMethod
     * @returns {Promise<{[key: string]: {entryId: number; entryLid: string;}>}}
     */
    getActiveSubscriptionsPageBaseMethod(start, pageSize, userMethod) {
        return userMethod.call(this).then((subscriberListObject) => {
            const entryList = Object.entries(subscriberListObject);
            const userList = entryList.map((d) => ({ userLid: d[0], entryLid: d[1] }));

            return userList.slice(start, start + pageSize);
        });
    }

    /**
     * @param {string} userLid
     * @param {string} entryLid
     */
    upsert(userLid, entryLid) {
        this.cachedData = this.upsertBaseMethod(userLid, entryLid, this.cachedData);
    }

    /**
     * @param {string} userLid
     * @param {string} entryLid
     */
    upsertVip(userLid, entryLid) {
        this.cachedVipData = this.upsertBaseMethod(userLid, entryLid, this.cachedVipData);
    }

    /**
     * @param {string} userLid
     * @param {string} entryLid
     * @param {[key: string]: string} cache
     * @returns {[key: string]: string}
     */
    upsertBaseMethod(userLid, newEntryLid, cache) {
        const oldEntryLid = cache[userLid] || '';
        if (this.letterboxdLidComparison.compare(oldEntryLid, newEntryLid) === 1) {
            return Object.assign({}, cache, { [userLid]: newEntryLid });
        }

        return cache;
    }
}
