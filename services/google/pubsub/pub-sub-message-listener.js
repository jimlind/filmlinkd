'use strict';

class PubSubMessageListener {
    constructor(pubSubConnection) {
        this.pubSubConnection = pubSubConnection;
    }

    onMessage(callback) {
        this.pubSubConnection.getSubscription().then((subsciption) => {
            subsciption.on('message', callback);
        });
    }
}

module.exports = PubSubMessageListener;
