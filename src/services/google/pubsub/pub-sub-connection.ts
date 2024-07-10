export default class PubSubConnection {
    /** @type {import('@google-cloud/pubsub').Topic[]} */
    topicList: any = [];
    /** @type {import('@google-cloud/pubsub').Subscription[]} */
    subscriptionList: any = [];
    /** @type boolean[] */
    getTopicLockedList: any = [];
    /** @type boolean[] */
    getSubscriptionLockedList: any = [];

    /**
     * @param {import('convict').Config} config
     * @param {import('@google-cloud/pubsub').PubSub} pubSub
     * @param {import('discord.js').Client} discordClient
     */
    constructor(readonly config: any, readonly pubSub: any, readonly discordClient: any) {}

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
     * Pub/Sub topic for announcing commands
     *
     * @returns {Promise<import('@google-cloud/pubsub').Topic>}
     */
    getCommandTopic() {
        return this.getTopic(this.config.get('pubSub.command.topicName'));
    }

    /**
     * Pub/Sub subscription for announcing commands
     * Currently configured for single listener because scraper is only instance that will be listening
     *
     * @returns {Promise<import('@google-cloud/pubsub').Subscription>}
     */
    getCommandSubscription() {
        return this.getSubscription(
            this.config.get('pubSub.command.topicName'),
            this.config.get('pubSub.command.subscriptionName'),
            false,
        );
    }

    /**
     * Close Pub/Sub subscription for announcing Log Entry writing results
     */
    closeCommandSubscription() {
        this.closeSubscription(this.config.get('pubSub.command.subscriptionName'), false);
    }

    /**
     * Close all Pub/Sub subscriptions
     */
    closeAllSubscriptions() {
        this.closeLogEntrySubscription();
        this.closeCommandSubscription();
    }

    /**
     * @param {string} topicName
     *
     * @returns {Promise<import('@google-cloud/pubsub').Topic>}
     */
    getTopic(topicName: any) {
        return new Promise((resolve, reject) => {
            // If topic is already available use it
            if (this.topicList[topicName]) {
                return resolve(this.topicList[topicName]);
            }

            // If this topic is locked wait until the topic is set
            if (this.getTopicLockedList[topicName]) {
                // Mostly arbitrary timing of how long it takes on my dev environment
                const delay = 140;
                const getTopic = () => {
                    if (this.topicList[topicName]) {
                        resolve(this.topicList[topicName]);
                    } else {
                        setTimeout(getTopic, delay);
                    }
                };
                setTimeout(getTopic, delay);
            } else {
                // Lock this method if not available and not already locked
                this.getTopicLockedList[topicName] = true;

                // Get or create topic
                const topic = this.pubSub.topic(topicName);
                topic
                    .exists()
                    .then(([exists]: any) => {
                        if (exists) {
                            this.topicList[topicName] = topic;
                            return resolve(topic);
                        } else {
                            topic
                                .create()
                                .then(([result]: any) => {
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
            }
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
    getSubscription(topicName: any, subscriptionName: any, shard = false) {
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
                .then((topic: any) => {
                    const subscription = topic.subscription(subscriptionName);
                    subscription
                        .exists()
                        .then(([exists]: any) => {
                            if (exists) {
                                this.subscriptionList[subscriptionName] = subscription;
                                return resolve(subscription);
                            } else {
                                const options = {
                                    messageRetentionDuration: { seconds: 43200 },
                                    expirationPolicy: { ttl: { seconds: 86400 } },
                                };
                                subscription.create(options).then(([result]: any) => {
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
    closeSubscription(subscriptionName: any, shard: any) {
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
    getFullSubscriptionName(name: any, shard: any) {
        if (!shard) {
            return name;
        }

        const shardList = this.discordClient?.options?.shards;
        const shardString = shardList.map((id: any) => String(id).padStart(3, '0')).join('-');
        return `${name}-shard-${shardString}`;
    }
}
