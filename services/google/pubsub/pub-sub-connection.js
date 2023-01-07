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
     * @param {import('discord.js').Client} discordClient
     */
    constructor(config, pubSub, discordClient) {
        this.config = config;
        this.pubSub = pubSub;
        this.discordClient = discordClient;
    }

    /**
     * Pub/Sub topic for announcing Log Entries
     *
     * @returns {Promise<import('@google-cloud/pubsub').Topic>}
     */
    getLogEntryTopic() {
        return this.getTopic(this.config.pubSubLogEntryTopicName);
    }

    /**
     * Pub/Sub subscription for announcing Log Entries
     *
     * @returns {Promise<import('@google-cloud/pubsub').Subscription>}
     */
    getLogEntrySubscription() {
        return this.getSubscription(
            this.config.pubSubLogEntryTopicName,
            this.config.pubSubLogEntrySubscriptionName,
        );
    }

    /**
     * @param {string} topicName
     *
     * @returns {Promise<import('@google-cloud/pubsub').Topic>}
     */
    getTopic(topicName) {
        return new Promise((resolve, reject) => {
            if (this.topic) {
                return resolve(this.topic);
            }

            if (this.getTopicLocked) {
                return reject('Duplicating initial topic getting requests');
            }
            this.getTopicLocked = true;

            const topic = this.pubSub.topic(topicName);
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
     * @param {string} topicName
     * @param {string} subsciptionName
     *
     * @returns {Promise<import('@google-cloud/pubsub').Subscription>}
     */
    getSubscription(topicName, subsciptionName) {
        return new Promise((resolve, reject) => {
            if (this.subscription) {
                return resolve(this.subscription);
            }

            if (this.getSubscriptionLocked) {
                return reject('Duplicating initial subscription getting requests.');
            }
            this.getSubscriptionLocked = true;

            const shardId = String(this.discordClient?.shard?.ids?.[0] || 0).padStart(3, '0');
            const subscriptionNameFull = `${subsciptionName}-shard-${shardId}`;

            this.getTopic(topicName)
                .then((topic) => {
                    const subscription = topic.subscription(subscriptionNameFull);
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
