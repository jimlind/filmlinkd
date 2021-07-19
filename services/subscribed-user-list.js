'use strict';

class SubscribedUserList {
    /**
     * @type {{ userName: string; previousId: number;}[]}
     */
    cachedData = [];

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
     * @param {number} previousId
     * @returns {number} The current PreviousID for the user after upsert
     */
    upsert(userName, previousId) {
        for (let x = 0; x < this.cachedData.length; x++) {
            const user = this.cachedData[x];
            // If the user already exists
            if (user.userName === userName) {
                // If the Id is newer than the last one
                if (user.previousId < previousId) {
                    this.cachedData[x].previousId = previousId;
                }
                // Break the loop. User was found so method is complete.
                return this.cachedData[x].previousId;
            }
        }
        // User not found in the loop so add new record instead
        this.cachedData.push({
            userName,
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
     * @returns {{ userName: string; previousId: number;}}
     */
    get(userName) {
        for (let x = 0; x < this.cachedData.length; x++) {
            if (this.cachedData[x].userName === userName) {
                return this.cachedData[x];
            }
        }
        return { userName, previousId: 0 };
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
     * @returns {Promise<{ userName: string; previousId: number;}[]>}}
     */
    getActiveSubscriptionsPage(index, pageSize) {
        return new Promise((resolve) => {
            this.getAllActiveSubscriptions().then((subscriberList) => {
                return resolve(subscriberList.slice(index, index + pageSize));
            });
        });
    }

    /**
     * @returns {Promise<{ userName: string; previousId: number;}[]>}}
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
                            previousId: subscriber?.previous?.id || 0,
                        };
                    }, []);

                    return resolve(this.cachedData);
                });
            }
        });
    }
}

module.exports = SubscribedUserList;
