import { Message, Subscription } from '@google-cloud/pubsub';

export default class PubSubMessageListener {
    constructor(readonly pubSubConnection: any) {}

    /**
     * @param {function} callback
     */
    async onLogEntryMessage(callback: (message: Message) => void) {
        const subsciption: Subscription = await this.pubSubConnection.getLogEntrySubscription();
        subsciption.on('message', callback);
        return;
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
