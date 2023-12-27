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

    /** Function that Truncates A String to 2 characters */
    static truncate(str, length) {
        if (length === 0) return str;

        return str.length > length ? str.substr(0, length - 3) + '...' : str;
    }
}
