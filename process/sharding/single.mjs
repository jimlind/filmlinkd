import { ClusterClient, getInfo } from 'discord-hybrid-sharding';

class Single {
    constructor(container) {
        this.container = container;
        this.client = this.container.resolve('discordClient');
        this.client.cluster = new ClusterClient(this.client);
        this.client.options.shards = getInfo().SHARD_LIST;
        this.client.options.shardCount = getInfo().TOTAL_SHARDS;

        // Trigger clean up on task ending
        this.container.resolve('exitHook')(this.cleanUp.bind(this));
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
        // Create an array to store messages coming from the pubsub
        const logEntryMessageList = [];
        // Take messages off the array and process them.
        setInterval(async () => {
            const message = logEntryMessageList.shift();
            if (!message) {
                return;
            }

            // Build the diary entry model and attempt to write it.
            const diaryEntryFactory = this.container.resolve('diaryEntryFactory');
            const diaryEntry = Object.assign(diaryEntryFactory.create(), message?.entry);
            const diaryEntryWriter = this.container.resolve('diaryEntryWriter');

            let userModel = null;
            try {
                userModel = await diaryEntryWriter.validateAndWrite(diaryEntry, message?.channelId);
            } catch (error) {
                const message = `Error on diaryEntryWriter::validateAndWrite for '${diaryEntry?.filmTitle}' by '${diaryEntry?.userName}'`;
                this.container.resolve('logger').error(message, { error: e });
            }

            // Exit early if nothing returned from validate and write.
            // This shard may not have access to the channel needed for the user so this is
            // a normal and expected behavior and no logs are neccessary here.
            if (!userModel) {
                return;
            }

            // If the returning diary entry has available publishing data log it
            if (message?.entry?.updatedDate && message?.entry?.publishSource) {
                this.container.resolve('logger').info('Entry Publish Delay', {
                    delay: Date.now() - message.entry.updatedDate,
                    source: message.entry.publishSource,
                });
            }

            // Write to the database
            return this.container.resolve('firestorePreviousDao').update(userModel, diaryEntry);
        }, 100);

        // Listen for LogEntry PubSub messages posted and respond
        this.container.resolve('pubSubMessageListener').onLogEntryMessage((message) => {
            // Acknowledge the message immediatly to remove it from the queue
            // They systems will continue to seamlessly retry if the system fails at this point
            message.ack();
            // Push the JSON object to the message list
            logEntryMessageList.push(JSON.parse(message.data.toString()));
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
