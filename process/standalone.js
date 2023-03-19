const DiaryEntry = require('../models/diary-entry');

class Standalone {
    /**
     * @param {import('../dependency-injection-container')} container
     */
    constructor(container) {
        this.container = container;

        // Trigger clean up on task ending
        require('death')(this.cleanUp.bind(this));
    }

    run() {
        // Start Discord Interaction Listeners on connection
        this.container
            .resolve('discordConnection')
            .getConnectedClient()
            .then((discordClient) => {
                const message = `Discord Client Logged In on ${discordClient.guilds.cache.size} Servers`;
                this.container.resolve('logger').info(message);

                // Listen for discord interactions and respond
                this.container
                    .resolve('discordInteractionListener')
                    .onInteraction((interaction) =>
                        this.container.resolve('interactionTranslator').translate(interaction),
                    );
            });

        // Listen for LogEntry PubSub messages posted and respond
        this.container.resolve('pubSubMessageListener').onLogEntryMessage((message) => {
            message.ack();
            const returnData = JSON.parse(message.data.toString());
            const diaryEntry = Object.assign(new DiaryEntry(), returnData?.entry);
            this.container
                .resolve('diaryEntryWriter')
                .validateAndWrite(diaryEntry, returnData?.channelId)
                .catch(() => {
                    const message = `Error on diaryEntryWriter::validateAndWrite for '${diaryEntry?.filmTitle}' by '${diaryEntry?.userName}'`;
                    container.resolve('logger').error(message);
                });
        });
    }

    cleanUp() {
        // Close the Discord connection
        this.container
            .resolve('discordConnection')
            .getConnectedClient()
            .then((client) => client.destroy());
        // Close the PubSub connection for LogEntry
        this.container.resolve('pubSubConnection').closeLogEntrySubscription();
    }
}

module.exports = Standalone;
