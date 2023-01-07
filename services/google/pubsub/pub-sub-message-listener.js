'use strict';

const crypto = require('crypto');

class PubSubMessageListener {
    /** @type string[] */
    hashList = [];

    constructor(pubSubConnection) {
        this.pubSubConnection = pubSubConnection;
    }

    /**
     * @param {function} callback
     */
    onLogEntryMessage(callback) {
        this.pubSubConnection.getLogEntrySubscription().then((subsciption) => {
            subsciption.on('message', (message) => this.filterDuplicate(message, callback));
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

    /**
     * @param {import('@google-cloud/pubsub').Message} message
     * @param {function} callback
     */
    filterDuplicate(message, callback) {
        const hash = crypto.createHash('md5').update(message.data).digest('base64');
        if (this.hashList.includes(hash)) {
            // Skip the messages if something with an identical hash was already accepted
            message.ack();
        } else {
            // Add the message to the hash list, limit total hashes, and run the callback method
            this.hashList = this.hashList.slice(0, 9999);
            this.hashList.unshift(hash);
            callback(message);
        }
    }
}

module.exports = PubSubMessageListener;
