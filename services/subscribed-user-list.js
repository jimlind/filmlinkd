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

    upsert(userName, previousId) {
        for (let x = 0; x < this.cachedData.length; x++) {
            const user = this.cachedData[x];
            // If the user already exists
            if (user.userName === userName) {
                // If the Id is newer than the last one
                if (user.previousId < previousId) {
                    this.cachedData[x].previousId = previousId;
                }
                // Break the loop. Method is complete.
                return;
            }
        }
        // Not found in the loop so add new record instead
        this.cachedData.push({
            userName,
            previousId,
        });
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
