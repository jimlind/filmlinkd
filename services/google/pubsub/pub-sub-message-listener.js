'use strict';

class PubSubMessageListener {
    constructor(pubSubConnection) {
        this.pubSubConnection = pubSubConnection;
    }

    /**
     * @param {function} callback
     */
    onMessage(callback) {
        this.pubSubConnection.getSubscription().then((subsciption) => {
            subsciption.on('message', callback);
        });
    }
}

module.exports = PubSubMessageListener;
