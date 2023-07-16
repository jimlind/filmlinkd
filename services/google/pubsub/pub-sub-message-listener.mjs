export default class PubSubMessageListener {
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
    onCommandMessage(callback) {
        this.pubSubConnection.getCommandSubscription().then((subsciption) => {
            subsciption.on('message', callback);
        });
    }
}
