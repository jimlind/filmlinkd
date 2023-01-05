'use strict';

class PubSubConnection {
    /** @type {import('@google-cloud/pubsub').Topic | null} */
    topic = null;
    /** @type {import('@google-cloud/pubsub').Subscription | null} */
    subscription = null;
    /** @type boolean */
    getTopicLocked = false;
    /** @type boolean */
    getSubscriptionLocked = false;

    /**
     * @param {import('../../../models/config')} config
     * @param {import('@google-cloud/pubsub').PubSub} pubSub
     */
    constructor(config, pubSub) {
        this.config = config;
        this.pubSub = pubSub;
    }

    /**
     * @returns {Promise<import('@google-cloud/pubsub').Topic>}
     */
    getTopic() {
        return new Promise((resolve, reject) => {
            if (this.topic) {
                return resolve(this.topic);
            }

            if (this.getTopicLocked) {
                return reject('Duplicating initial topic getting requests');
            }
            this.getTopicLocked = true;

            const topic = this.pubSub.topic(this.config.pubSubTopicName);
            topic
                .exists()
                .then(([exists]) => {
                    if (exists) {
                        this.topic = topic;
                        return resolve(topic);
                    } else {
                        topic
                            .create()
                            .then(([result]) => {
                                this.topic = result;
                                return resolve(result);
                            })
                            .catch(() => {
                                return reject('Unable to create topic');
                            });
                    }
                })
                .catch(() => {
                    return reject('Unable to check topic exists');
                });
        });
    }

    /**
     * This method now problematically has state, but has always had order of operation issues
     * in the grand scheme of things.
     * On the first run the discord client is needed because it uses that to get the shard id to create
     * or use an existing topic subscription name
     * On later runs the discord client isn't needed because it grabs the locally cached version
     *
     * @param {import('discord.js').Client | null} discordClient
     *
     * @returns {Promise<import('@google-cloud/pubsub').Subscription>}
     */
    getSubscription(discordClient) {
        return new Promise((resolve, reject) => {
            if (this.subscription) {
                return resolve(this.subscription);
            }

            if (this.getSubscriptionLocked) {
                return reject('Duplicating initial subscription getting requests.');
            }
            this.getSubscriptionLocked = true;

            const shardId = String((discordClient?.shard?.ids || [0]).shift()).padStart(3, '0');
            const subscriptionName = `${this.config.pubSubSubscriptionName}-shard-${shardId}`;

            this.getTopic()
                .then((topic) => {
                    const subscription = topic.subscription(subscriptionName);
                    subscription
                        .exists()
                        .then(([exists]) => {
                            if (exists) {
                                this.subscription = subscription;
                                return resolve(subscription);
                            } else {
                                subscription.create().then(([result]) => {
                                    this.subscription = result;
                                    return resolve(result);
                                });
                            }
                        })
                        .catch(() => {
                            return reject('Unable to check subsciption exists');
                        });
                })
                .catch((e) => {
                    return reject('Unable to get topic: ' + e);
                });
        });
    }
}

module.exports = PubSubConnection;
