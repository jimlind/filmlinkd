export default class Standalone {
    /**
     * @param {import('../dependency-injection-container.mjs')} container
     */
    constructor(readonly container: any) {
        // Trigger clean up on task ending
        this.container.resolve('exitHook')(this.cleanUp.bind(this));
    }

    run() {
        // Start Discord Interaction Listeners on connection
        this.container
            .resolve('discordConnection')
            .getConnectedClient()
            .then((discordClient: any) => {
                const message = `Discord Client Logged In on ${discordClient.guilds.cache.size} Servers`;
                this.container.resolve('logger').info(message);

                // Listen for discord interactions and respond
                this.container
                    .resolve('discordInteractionListener')
                    .onInteraction((interaction: any) =>
                        this.container.resolve('interactionTranslator').translate(interaction),
                    );
            });

        // Listen for LogEntry PubSub messages posted and respond
        this.container.resolve('pubSubMessageListener').onLogEntryMessage((message: any) => {
            message.ack();

            const diaryEntryFactory = this.container.resolve('diaryEntryFactory');
            const diaryEntryWriter = this.container.resolve('diaryEntryWriter');

            const diaryEntry = diaryEntryFactory.createFromMessage(message);
            const channelId = JSON.parse(message.data.toString())?.channelId || '';

            // Does not write to the database on success
            diaryEntryWriter.validateAndWrite(diaryEntry, channelId).catch(() => {
                const message = `Error on diaryEntryWriter::validateAndWrite for '${diaryEntry?.filmTitle}' by '${diaryEntry?.userName}'`;
                this.container.resolve('logger').error(message);
            });
        });
    }

    cleanUp(signal: any) {
        // Log process closure
        this.container.resolve('logger').info('Standalone Process Terminated', { signal });
        // Close the Discord connection
        const clientPromise = this.container.resolve('discordConnection').getConnectedClient();
        clientPromise.then((client: any) => client.destroy());
        // Close any open PubSub connections
        this.container.resolve('pubSubConnection').closeAllSubscriptions();
    }
}
