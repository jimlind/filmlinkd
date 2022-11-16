'use strict';

class SubscribedUserList {
    /**
     * @type {{ userName: string; lid: string; previousId: number;}[]}
     */
    cachedData = [];

    /**
     * @type {[key: string]: {entryId: number; entryLid: string;} | null}
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
     * @param {string} userName
     * @param {string} lid
     * @param {number} previousId
     * @returns {number} The current PreviousID for the user after upsert
     */
    upsert(userName, lid, previousId) {
        const dataSource = this.cachedData;

        for (let x = 0; x < dataSource.length; x++) {
            const user = dataSource[x];
            // If the user already exists
            if (user.userName === userName) {
                // If the Id is newer than the last one
                if (user.previousId < previousId) {
                    dataSource[x].previousId = previousId;
                }
                // Break the loop. User was found so method is complete.
                return dataSource[x].previousId;
            }
        }
        // User not found in the loop so add new record instead
        dataSource.push({
            userName,
            lid,
            previousId,
        });
        return previousId;
    }

    /**
     * @param {string} userLid
     * @param {number} entryId
     * @param {string} entryLid
     */
    upsertVip(userId, entryId, entryLid) {
        const cache = this.cachedVipData;
        const currentData = cache[userId];
        const newData = {
            entryId: entryId || 0,
            entryLid: entryLid || '',
        };

        if (currentData.entryLid) {
            if (this.letterboxdLidComparison.compare(currentData.entryLid, entryLid) === 1) {
                cache[userId] = newData;
            }
            return;
        }

        if (currentData.entryId < entryId) {
            cache[userId] = newData;
        }
    }

    remove(userName) {
        for (let x = 0; x < this.cachedData.length; x++) {
            if (this.cachedData[x].userName === userName) {
                this.cachedData.splice(x, 1);
                break;
            }
        }
        //TODO: Does it make sense to call firestore here?
    }

    /**
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
                return resolve(Math.floor(Math.random() * subscriberList.length));
            });
        });
    }

    /**
     * @param {number} index
     * @param {number} pageSize
     * @returns {Promise<{ userName: string; lid: string, previousId: number;}[]>}}
     */
    getActiveSubscriptionsPage(index, pageSize) {
        return new Promise((resolve) => {
            this.getAllActiveSubscriptions().then((subscriberList) => {
                return resolve(subscriberList.slice(index, index + pageSize));
            });
        });
    }

    /**
     * @param {number} index The starting index assuming zero-indexed array
     * @param {number} pageSize The total number of entries returned
     * @returns {Promise<{[key: string]: {entryId: number; entryLid: string;}>}}
     */
    getActiveVipSubscriptionsPage(index, pageSize) {
        return new Promise((resolve) => {
            this.getVipActiveSubscriptions().then((vipList) => {
                const returnData = {};
                let count = 0;
                for (const key in vipList) {
                    if (count < index) {
                        continue;
                    }

                    returnData[key] = vipList[key];
                    count++;

                    if (count == index + pageSize) {
                        break;
                    }
                }
                return resolve(returnData);
            });
        });
    }

    /**
     * @returns {Promise<{ userName: string; lid: string; previousId: number;}[]>}}
     */
    getAllActiveSubscriptions() {
        return new Promise((resolve) => {
            if (this.cachedData.length) {
                return resolve(this.cachedData);
            } else {
                this.firestoreSubscriptionDao.getActiveSubscriptions().then((subscriberList) => {
                    this.logger.info('Loaded and Cached Users', {
                        userCount: subscriberList.length,
                    });

                    this.cachedData = subscriberList.map((subscriber) => {
                        return {
                            userName: subscriber.userName,
                            lid: subscriber.letterboxdId,
                            previousId: subscriber?.previous?.id || 0,
                        };
                    }, []);

                    return resolve(this.cachedData);
                });
            }
        });
    }

    /**
     * @returns {Promise<{[key: string]: {entryId: number; entryLid: string;}>}}
     */
    getVipActiveSubscriptions() {
        return new Promise((resolve) => {
            if (this.cachedVipData) {
                return resolve(this.cachedVipData);
            } else {
                this.cachedVipData = {};
                this.firestoreSubscriptionDao.getVipSubscriptions().then((vipList) => {
                    this.logger.info('Loaded and Cached VIPs', {
                        userCount: vipList.length,
                    });

                    this.cachedVipData = {};
                    vipList.forEach((vip) => {
                        this.cachedVipData[vip.letterboxdId] = {
                            entryId: vip?.previous?.id || 0,
                            entryLid: vip?.previous?.lid || '',
                        };
                    });

                    return resolve(this.cachedVipData);
                });
            }
        });
    }
}

module.exports = SubscribedUserList;
