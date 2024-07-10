export default class PubSubMessageListener {
    constructor(readonly pubSubConnection: any) {}

    /**
     * @param {function} callback
     */
    onLogEntryMessage(callback: any) {
        return this.pubSubConnection
            .getLogEntrySubscription()
            .then((subsciption: any) => subsciption.on('message', callback));
    }

    /**
     * @param {function} callback
     */
    onCommandMessage(callback: any) {
        return this.pubSubConnection
            .getCommandSubscription()
            .then((subsciption: any) => subsciption.on('message', callback));
    }
}
