'use strict';

class PubSubMessageListener {
    constructor(pubSubConnection) {
        this.pubSubConnection = pubSubConnection;
    }

    /**
     * @param {function} callback
     */
    onLogEntryMessage(callback) {
        this.pubSubConnection.getLogEntrySubscription().then((subsciption) => {
            subsciption.on('message', callback);
        });
    }

    /**
     * @param {function} callback
     */
    onLogEntryResultMessage(callback) {
        this.pubSubConnection.getLogEntryResultSubscription().then((subsciption) => {
            subsciption.on('message', callback);
        });
    }
}

module.exports = PubSubMessageListener;
