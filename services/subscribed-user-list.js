'use strict';

class SubscribedUserList {
    /**
     * @type {{ userName: string; lid: string; previousId: number;}[]}
     */
    cachedData = [];

    /**
     * @type {{ userName: string; lid: string; previousId: number;}[]}
     */
    cachedVipData = [];

    /**
     * @param {import('./google/firestore/firestore-subscription-dao')} firestoreSubscriptionDao
     * @param {import('./logger')} logger
     */
    constructor(firestoreSubscriptionDao, logger) {
        this.firestoreSubscriptionDao = firestoreSubscriptionDao;
        this.logger = logger;
    }

    /**
     * @param {string} userName
     * @param {string} lid
     * @param {number} previousId
     * @param {boolean} vipStatus
     * @returns {number} The current PreviousID for the user after upsert
     */
    upsert(userName, lid, previousId, vipStatus = false) {
        const dataSource = vipStatus ? this.cachedVipData : this.cachedData;

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
     * @param {number} index
     * @param {number} pageSize
     * @returns {Promise<{ userName: string; lid: string; previousId: number;}[]>}}
     */
    getActiveVipSubscriptionsPage(index, pageSize) {
        return new Promise((resolve) => {
            this.getVipActiveSubscriptions().then((vipList) => {
                return resolve(vipList.slice(index, index + pageSize));
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
     * @returns {Promise<{ userName: string; lid: string; previousId: number;}[]>}}
     */
    getVipActiveSubscriptions() {
        return new Promise((resolve) => {
            if (this.cachedVipData.length) {
                return resolve(this.cachedVipData);
            } else {
                this.firestoreSubscriptionDao.getVipSubscriptions().then((vipList) => {
                    this.logger.info('Loaded and Cached VIPs', {
                        userCount: vipList.length,
                    });

                    this.cachedVipData = vipList.map((vip) => {
                        return {
                            userName: vip.userName,
                            lid: vip.letterboxdId,
                            previousId: vip?.previous?.id || 0,
                        };
                    }, []);

                    return resolve(this.cachedVipData);
                });
            }
        });
    }
}

module.exports = SubscribedUserList;
