'use strict';

class PubSubConnection {
    /** @type {import('@google-cloud/pubsub').Topic[]} */
    topicList = [];
    /** @type {import('@google-cloud/pubsub').Subscription[]} */
    subscriptionList = [];
    /** @type boolean[] */
    getTopicLockedList = [];
    /** @type boolean[] */
    getSubscriptionLockedList = [];

    /**
     * @param {import('convict').Config} config
     * @param {import('@google-cloud/pubsub').PubSub} pubSub
     * @param {import('discord.js').Client} discordClient
     */
    constructor(config, pubSub, discordClient) {
        this.config = config;
        this.pubSub = pubSub;
        this.discordClient = discordClient;
    }

    /**
     * Pub/Sub topic for announcing Log Entries for writing
     *
     * @returns {Promise<import('@google-cloud/pubsub').Topic>}
     */
    getLogEntryTopic() {
        return this.getTopic(this.config.get('pubSub.logEntry.topicName'));
    }

    /**
     * Pub/Sub subscription for announcing Log Entries for writing
     * Configured for multiple listeners
     *
     * @returns {Promise<import('@google-cloud/pubsub').Subscription>}
     */
    getLogEntrySubscription() {
        return this.getSubscription(
            this.config.get('pubSub.logEntry.topicName'),
            this.config.get('pubSub.logEntry.subscriptionName'),
            true,
        );
    }

    /**
     * Close Pub/Sub subscription for announcing Log Entries for writing
     */
    closeLogEntrySubscription() {
        this.closeSubscription(this.config.get('pubSub.logEntry.subscriptionName'), true);
    }

    /**
     * Pub/Sub topic for announcing Log Entry writing results
     *
     * @returns {Promise<import('@google-cloud/pubsub').Topic>}
     */
    getLogEntryResultTopic() {
        return this.getTopic(this.config.get('pubSub.logEntryResult.topicName'));
    }

    /**
     * Pub/Sub subscription for announcing Log Entry writing results
     * Configured for single listener
     *
     * @returns {Promise<import('@google-cloud/pubsub').Subscription>}
     */
    getLogEntryResultSubscription() {
        return this.getSubscription(
            this.config.get('pubSub.logEntryResult.topicName'),
            this.config.get('pubSub.logEntryResult.subscriptionName'),
            false,
        );
    }

    /**
     * Close Pub/Sub subscription for announcing Log Entry writing results
     */
    closeLogEntryResultSubscription() {
        this.closeSubscription(this.config.get('pubSub.logEntryResult.subscriptionName'), false);
    }

    /**
     * Pub/Sub topic for announcing select user commands
     * Not Used; I thought I'd want this, but I haven't found a need for it yet.
     *
     * @returns {Promise<import('@google-cloud/pubsub').Topic>}
     */
    getCommandTopic() {
        return this.getTopic(this.config.get('pubSub.command.topicName'));
    }

    /**
     * Pub/Sub subscription for announcing select user commands
     * Not Used; I thought I'd want this, but I haven't found a need for it yet.
     * Configured for multiple listeners
     *
     * @returns {Promise<import('@google-cloud/pubsub').Subscription>}
     */
    getCommandSubscription() {
        return this.getSubscription(
            this.config.get('pubSub.command.topicName'),
            this.config.get('pubSub.command.subscriptionName'),
            true,
        );
    }

    /**
     * TODO: Introduce a lock to avoid multiple connections.
     * TODO: Can this be cleaned up?
     *
     * @param {string} topicName
     *
     * @returns {Promise<import('@google-cloud/pubsub').Topic>}
     */
    getTopic(topicName) {
        return new Promise((resolve, reject) => {
            if (this.topicList[topicName]) {
                return resolve(this.topicList[topicName]);
            }

            if (this.getTopicLockedList[topicName]) {
                return reject('Duplicating initial topic getting requests');
            }
            this.getTopicLockedList[topicName] = true;

            const topic = this.pubSub.topic(topicName);
            topic
                .exists()
                .then(([exists]) => {
                    if (exists) {
                        this.topicList[topicName] = topic;
                        return resolve(topic);
                    } else {
                        topic
                            .create()
                            .then(([result]) => {
                                this.topicList[topicName] = result;
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
     * TODO: Introduce a lock to avoid multiple connections.
     * TODO: Can this be cleaned up?
     * TODO: Some way to do a better write up of how sharding works here?
     *
     * @param {string} topicName
     * @param {string} subscriptionName
     * @param {boolean} shard
     *
     * @returns {Promise<import('@google-cloud/pubsub').Subscription>}
     */
    getSubscription(topicName, subscriptionName, shard = false) {
        subscriptionName = this.getFullSubscriptionName(subscriptionName, shard);
        return new Promise((resolve, reject) => {
            if (this.subscriptionList[subscriptionName]) {
                return resolve(this.subscriptionList[subscriptionName]);
            }

            if (this.getSubscriptionLockedList[subscriptionName]) {
                return reject('Duplicating initial subscription getting requests.');
            }
            this.getSubscriptionLockedList[subscriptionName] = true;

            this.getTopic(topicName)
                .then((topic) => {
                    const subscription = topic.subscription(subscriptionName);
                    subscription
                        .exists()
                        .then(([exists]) => {
                            if (exists) {
                                this.subscriptionList[subscriptionName] = subscription;
                                return resolve(subscription);
                            } else {
                                subscription.create().then(([result]) => {
                                    this.subscriptionList[subscriptionName] = result;
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

    /**
     *
     * @param {string} subscriptionName
     * @param {boolean} shard
     * @returns
     */
    closeSubscription(subscriptionName, shard) {
        subscriptionName = this.getFullSubscriptionName(subscriptionName, shard);
        const subscription = this.subscriptionList[subscriptionName];

        if (!subscription) {
            return;
        }

        subscription.close();
    }

    /**
     *
     * @param {string} name
     * @param {boolean} shard
     */
    getFullSubscriptionName(name, shard) {
        if (!shard) {
            return name;
        }

        const shardId = String(this.discordClient?.shard?.ids?.[0] || 0).padStart(3, '0');
        return `${name}-shard-${shardId}`;
    }
}

module.exports = PubSubConnection;
