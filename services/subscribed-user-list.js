'use strict';

class SubscribedUserList {
    /**
     * @type {[key: string]: string} | null}
     */
    cachedData = null;

    /**
     * @type {[key: string]: string} | null}
     */
    cachedVipData = null;

    /**
     * @param {import('./google/firestore/firestore-subscription-dao')} firestoreSubscriptionDao
     * @param {import('./letterboxd/letterboxd-lid-comparison')} letterboxdLidComparison
     * @param {import('./logger')} logger
     */
    constructor(firestoreSubscriptionDao, letterboxdLidComparison, logger) {
        this.firestoreSubscriptionDao = firestoreSubscriptionDao;
        this.letterboxdLidComparison = letterboxdLidComparison;
        this.logger = logger;
    }

    /**
     * @param {string} userLid
     * @param {string} entryLid
     */
    upsert(userLid, entryLid) {
        const oldEntryLid = this.cachedData[userLid] || '';

        if (this.letterboxdLidComparison.compare(oldEntryLid, entryLid) === 1) {
            this.cachedData[userLid] = entryLid;
        }
    }

    /**
     * @param {string} userLid
     * @param {string} entryLid
     */
    upsertVip(userLid, entryLid) {
        const oldEntryLid = this.cachedVipData[userLid] || '';

        if (this.letterboxdLidComparison.compare(oldEntryLid, entryLid) === 1) {
            this.cachedVipData[userLid] = entryLid;
        }
    }

    /**
     * This is gonna be seriously broken.
     * I dont think it is even used any more. Need to explore.
     *
     * @param {string} userName
     * @returns {{ userName: string; lid: string, previousId: number;}}
     */
    get(userName) {
        for (let x = 0; x < this.cachedData.length; x++) {
            if (this.cachedData[x].userName === userName) {
                return this.cachedData[x];
            }
        }
        return { userName, lid: '', previousId: 0 };
    }

    getRandomIndex() {
        return new Promise((resolve) => {
            this.getAllActiveSubscriptions().then((subscriberList) => {
                return resolve(Math.floor(Math.random() * Object.values(subscriberList).length));
            });
        });
    }

    /**
     * @param {number} index
     * @param {number} pageSize
     * @returns {Promise<{ userLid: string; entryLid: string}[]>}}
     */
    getActiveSubscriptionsPage(index, pageSize) {
        return new Promise((resolve) => {
            this.getAllActiveSubscriptions().then((subscriberListObject) => {
                const entryList = Object.entries(subscriberListObject);
                const userList = entryList.map((d) => ({ userLid: d[0], entryLid: d[1] }));

                return resolve(userList.slice(index, index + pageSize));
            });
        });
    }

    /**
     * @param {number} start The starting index assuming zero-indexed array
     * @param {number} pageSize The total number of entries returned
     * @returns {Promise<{[key: string]: {entryId: number; entryLid: string;}>}}
     */
    getActiveVipSubscriptionsPage(start, pageSize) {
        return new Promise((resolve) => {
            this.getVipActiveSubscriptions().then((vipList) => {
                const returnData = {};
                let index = 0;
                let count = 0;
                for (const key in vipList) {
                    index++;
                    if (index <= start) {
                        continue;
                    }

                    returnData[key] = vipList[key];
                    count++;

                    if (count >= pageSize) {
                        break;
                    }
                }
                return resolve(returnData);
            });
        });
    }

    /**
     * @returns {Promise<{[key: string]: string>}}
     */
    getAllActiveSubscriptions() {
        return new Promise((resolve) => {
            if (this.cachedData) {
                return resolve(this.cachedData);
            } else {
                this.cachedData = {};
                this.firestoreSubscriptionDao.getActiveSubscriptions().then((userList) => {
                    const data = { userCount: userList.length };
                    this.logger.info('Loaded and Cached Users', data);

                    this.cachedData = {};
                    userList.forEach((user) => {
                        this.cachedData[user.letterboxdId] = user?.previous?.lid || '';
                    });

                    return resolve(this.cachedData);
                });
            }
        });
    }

    /**
     * @returns {Promise<{[key: string]: string>}}
     */
    getVipActiveSubscriptions() {
        return new Promise((resolve) => {
            if (this.cachedVipData) {
                return resolve(this.cachedVipData);
            } else {
                this.cachedVipData = {};
                this.firestoreSubscriptionDao.getVipSubscriptions().then((vipList) => {
                    const data = { userCount: vipList.length };
                    this.logger.info('Loaded and Cached VIPs', data);

                    this.cachedVipData = {};
                    vipList.forEach((vip) => {
                        this.cachedVipData[vip.letterboxdId] = vip?.previous?.lid || '';
                    });

                    return resolve(this.cachedVipData);
                });
            }
        });
    }
}

module.exports = SubscribedUserList;
