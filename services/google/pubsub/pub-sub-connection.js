'use strict';

class PubSubConnection {
    topic = null;
    subscription = null;
    getTopicLocked = false;
    getSubscriptionLocked = false;

    constructor(config, pubSub) {
        this.config = config;
        this.pubSub = pubSub;
    }

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

    getSubscription() {
        return new Promise((resolve, reject) => {
            if (this.subscription) {
                return resolve(this.subscription);
            }

            if (this.getSubscriptionLocked) {
                return reject('Duplicating initial subscription getting requests.');
            }
            this.getSubscriptionLocked = true;

            this.getTopic()
                .then((topic) => {
                    const subscription = topic.subscription(this.config.pubSubSubscriptionName);
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
