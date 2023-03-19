const { getInfo } = require('discord-hybrid-sharding');
const DiaryEntry = require('../../models/diary-entry');

class Single {
    constructor(container) {
        this.container = container;
        this.client = this.container.resolve('discordClient');
        this.client.options.shards = getInfo().SHARD_LIST;
        this.client.options.shardCount = getInfo().TOTAL_SHARDS;

        // Trigger clean up on task ending
        require('death')(this.cleanUp.bind(this));
    }

    run() {
        // Setup the connected client and listen for interactions
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
            // Acknowledge the message immediatly to remove it from the queue
            // They systems will continue to seamlessly retry if the system fails at this point
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

const config = require('../../config');
const container = require('../../dependency-injection-container')(config);
const single = new Single(container);
single.run();
