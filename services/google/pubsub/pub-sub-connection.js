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
     *
     * @returns {Promise<import('@google-cloud/pubsub').Subscription>}
     */
    getLogEntrySubscription() {
        return this.getSubscription(
            this.config.get('pubSub.logEntry.topicName'),
            this.config.get('pubSub.logEntry.subscriptionName'),
        );
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
     *
     * @returns {Promise<import('@google-cloud/pubsub').Subscription>}
     */
    getLogEntryResultSubscription() {
        return this.getSubscription(
            this.config.get('pubSub.logEntryResult.topicName'),
            this.config.get('pubSub.logEntryResult.subscriptionName'),
        );
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
     *
     * @returns {Promise<import('@google-cloud/pubsub').Subscription>}
     */
    getCommandSubscription() {
        return this.getSubscription(
            this.config.get('pubSub.command.topicName'),
            this.config.get('pubSub.command.subscriptionName'),
        );
    }

    /**
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
     * @param {string} topicName
     * @param {string} subsciptionName
     *
     * @returns {Promise<import('@google-cloud/pubsub').Subscription>}
     */
    getSubscription(topicName, subsciptionName) {
        return new Promise((resolve, reject) => {
            if (this.subscriptionList[subsciptionName]) {
                return resolve(this.subscriptionList[subsciptionName]);
            }

            if (this.getSubscriptionLockedList[subsciptionName]) {
                return reject('Duplicating initial subscription getting requests.');
            }
            this.getSubscriptionLockedList[subsciptionName] = true;

            const shardId = String(this.discordClient?.shard?.ids?.[0] || 0).padStart(3, '0');
            const subscriptionNameFull = `${subsciptionName}-shard-${shardId}`;

            this.getTopic(topicName)
                .then((topic) => {
                    const subscription = topic.subscription(subscriptionNameFull);
                    subscription
                        .exists()
                        .then(([exists]) => {
                            if (exists) {
                                this.subscriptionList[subsciptionName] = subscription;
                                return resolve(subscription);
                            } else {
                                subscription.create().then(([result]) => {
                                    this.subscriptionList[subsciptionName] = result;
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
