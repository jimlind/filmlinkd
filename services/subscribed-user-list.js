'use strict';

class SubscribedUserList {
    /**
     * @type {{ userName: string; lid: string; previousId: number;}[]}
     */
    cachedData = [];

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
     * Add new users to the Subscribed User List or update existing user data if it
     * already exists
     *
     * @param {string} userName   Letterboxd Username
     * @param {string} lid        Letterboxd User Id
     * @param {number} previousId Letterboxd Diary Entry Id
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
     * @param {string} entryLid
     */
    upsertVip(userId, entryLid) {
        const oldEntryLid = this.cachedVipData[userId] || '';

        if (this.letterboxdLidComparison.compare(oldEntryLid, entryLid) === 1) {
            this.cachedVipData[userId] = entryLid;
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

                    // We could probably use the this.upsert method to properly "dog food" and use
                    // the same method internally and externally, but this is a bit of a shortcut
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
