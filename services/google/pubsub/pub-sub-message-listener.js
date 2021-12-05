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
    onMessage(callback) {
        this.hashList = this.hashList.slice(0, 999);
        this.pubSubConnection.getSubscription().then((subsciption) => {
            subsciption.on('message', (message) => this.filterDuplicate(message, callback));
        });
    }
    /**
     * @param {import('@google-cloud/pubsub').Message} message
     * @param {function} callback
     */
    filterDuplicate(message, callback) {
        const hash = crypto.createHash('md5').update(message.data).digest('base64');
        if (this.hashList.includes(hash)) {
            // Skip the messages if something with an identical has was already accepted
            message.ack();
        } else {
            // Add the messase to the hash list, and run the callback method
            this.hashList.unshift(hash);
            callback(message);
        }
    }
}

module.exports = PubSubMessageListener;
