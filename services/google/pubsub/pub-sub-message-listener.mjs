export default class PubSubMessageListener {
    constructor(pubSubConnection) {
        this.pubSubConnection = pubSubConnection;
    }

    /**
     * @param {function} callback
     */
    onLogEntryMessage(callback) {
        return this.pubSubConnection
            .getLogEntrySubscription()
            .then((subsciption) => subsciption.on('message', callback));
    }

    /**
     * @param {function} callback
     */
    onCommandMessage(callback) {
        return this.pubSubConnection
            .getCommandSubscription()
            .then((subsciption) => subsciption.on('message', callback));
    }
}
