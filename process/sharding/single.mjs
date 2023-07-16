import { ClusterClient, getInfo } from 'discord-hybrid-sharding';

class Single {
    constructor(container) {
        this.container = container;
        this.client = this.container.resolve('discordClient');
        this.client.cluster = new ClusterClient(this.client);
        this.client.options.shards = getInfo().SHARD_LIST;
        this.client.options.shardCount = getInfo().TOTAL_SHARDS;

        // Trigger clean up on task ending
        this.container.resolve('death')(this.cleanUp.bind(this));
    }

    run() {
        // Setup the connected client and listen for interactions
        this.container
            .resolve('discordConnection')
            .getConnectedClient()
            .then((discordClient) => {
                const message = `Discord Client Logged In on ${discordClient.guilds.cache.size} Servers`;
                this.container.resolve('logger').info(message);

                this.startInteractionListener();
                this.startPubSubMessageListener();
            });
    }

    startInteractionListener() {
        // Listen for discord interactions and respond
        this.container
            .resolve('discordInteractionListener')
            .onInteraction((interaction) =>
                this.container.resolve('interactionTranslator').translate(interaction),
            );
    }

    startPubSubMessageListener() {
        // Listen for LogEntry PubSub messages posted and respond
        this.container.resolve('pubSubMessageListener').onLogEntryMessage((message) => {
            // Acknowledge the message immediatly to remove it from the queue
            // They systems will continue to seamlessly retry if the system fails at this point
            message.ack();
            const diaryEntryFactory = this.container.resolve('diaryEntryFactory');
            const returnData = JSON.parse(message.data.toString());
            const diaryEntry = Object.assign(diaryEntryFactory.create(), returnData?.entry);
            this.container
                .resolve('diaryEntryWriter')
                .validateAndWrite(diaryEntry, returnData?.channelId)
                .then(([userModel, viewingId]) => {
                    // Exit early if no values from validate and write.
                    if (!userModel || !viewingId) {
                        return Promise.all([]);
                    }

                    // Override id because not always set
                    diaryEntry.id = viewingId;

                    // Exit early if the existing diary entry id is older than the incoming diary entry id
                    if ((userModel?.previous?.id || 0) >= viewingId) {
                        return Promise.all([]);
                    }

                    // If the returning diary entry has available publishing data log it
                    if (returnData?.entry?.updatedDate && returnData?.entry?.publishSource) {
                        this.container.resolve('logger').info('Entry Publish Delay', {
                            delay: Date.now() - returnData.entry.updatedDate,
                            source: returnData.entry.publishSource,
                        });
                    }

                    // Write to the database
                    return this.container
                        .resolve('firestorePreviousDao')
                        .update(userModel, diaryEntry);
                })
                .catch(() => {
                    const message = `Error on diaryEntryWriter::validateAndWrite for '${diaryEntry?.filmTitle}' by '${diaryEntry?.userName}'`;
                    this.container.resolve('logger').error(message);
                });
        });
    }

    cleanUp() {
        // Ensure all the PubSub connections are closed
        this.container.resolve('pubSubConnection').closeAllSubscriptions();

        // Close the Discord connection and exit the process
        this.container
            .resolve('discordConnection')
            .getConnectedClient()
            .then((client) => {
                client.destroy();
                // Force a process exit due to how the discord-hybrid-sharding library works
                process.exit();
            });
    }
}

import config from '../../config.mjs';
import container from '../../dependency-injection-container.mjs';
container(config)
    .initialize()
    .then((awilixContainer) => {
        new Single(awilixContainer).run();
    });
