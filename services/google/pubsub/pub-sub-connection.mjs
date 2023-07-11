export default class PubSubConnection {
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
     * Close all Pub/Sub subscriptions
     */
    closeAllSubscriptions() {
        this.closeLogEntrySubscription();
        this.closeLogEntryResultSubscription();
    }

    /**
     * @param {string} topicName
     *
     * @returns {Promise<import('@google-cloud/pubsub').Topic>}
     */
    getTopic(topicName) {
        return new Promise((resolve, reject) => {
            // If topic is already available use it
            if (this.topicList[topicName]) {
                return resolve(this.topicList[topicName]);
            }

            // If this method is locked wait until the topic is set
            if (this.getTopicLockedList[topicName]) {
                const interval = setInterval(() => {
                    if (!this.getTopicLockedList[topicName]) {
                        clearInterval(interval);
                        resolve(this.topicList[topicName]);
                    }
                }, 140); // Mostly arbitrary timing of how long it takes on my dev environment
            }
            // Lock this method if not available and not already locked
            this.getTopicLockedList[topicName] = true;

            // Get or create topic
            const topic = this.pubSub.topic(topicName);
            topic
                .exists()
                .then(([exists]) => {
                    if (exists) {
                        this.topicList[topicName] = topic;
                        this.getTopicLockedList[topicName] = false;
                        return resolve(topic);
                    } else {
                        topic
                            .create()
                            .then(([result]) => {
                                this.topicList[topicName] = result;
                                this.getTopicLockedList[topicName] = false;
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
                                const options = {
                                    messageRetentionDuration: { seconds: 43200 },
                                    expirationPolicy: { ttl: { seconds: 86400 } },
                                };
                                subscription.create(options).then(([result]) => {
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

        const shardList = this.discordClient?.options?.shards;
        const shardString = shardList.map((id) => String(id).padStart(3, '0')).join('-');
        return `${name}-shard-${shardString}`;
    }
}
