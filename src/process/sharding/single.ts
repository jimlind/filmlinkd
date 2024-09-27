import { AwilixContainer } from 'awilix';
import { ClusterClient, getInfo } from 'discord-hybrid-sharding';

class Single {
    client: any;

    constructor(readonly container: any) {
        this.client = container.resolve('discordClient');
        this.client.cluster = new ClusterClient(this.client);
        this.client.options.shards = getInfo().SHARD_LIST;
        this.client.options.shardCount = getInfo().TOTAL_SHARDS;

        // Trigger clean up on task ending
        container.resolve('exitHook')(this.cleanUp.bind(this));
    }

    run() {
        // Setup the connected client and listen for interactions
        this.container
            .resolve('discordConnection')
            .getConnectedClient()
            .then((discordClient: any) => {
                const message = `Discord Client Logged In on ${discordClient.guilds.cache.size} Servers`;
                this.container.resolve('logger').info(message);

                this.startInteractionListener();
                // this.startPubSubMessageListener();
            });
    }

    startInteractionListener() {
        // Listen for discord interactions and respond
        this.container
            .resolve('discordInteractionListener')
            .onInteraction((interaction: any) =>
                this.container.resolve('interactionTranslator').translate(interaction),
            );
    }

    startPubSubMessageListener() {
        // Create an array to store messages coming from the pubsub
        const logEntryMessageList: any[] = [];

        // Take messages off the array and process them.
        // Create the function and use it. Normally I'd use arrow functions but trying to track down a memory issue.
        async function processOneMessageListEntry(container: AwilixContainer) {
            const message = logEntryMessageList.shift();
            if (!message) {
                return;
            }

            // Build the diary entry model and attempt to write it.
            const diaryEntryFactory = container.resolve('diaryEntryFactory');
            const diaryEntry = Object.assign(diaryEntryFactory.create(), message?.entry);
            const diaryEntryWriter = container.resolve('diaryEntryWriter');

            let userModel = null;
            try {
                userModel = await diaryEntryWriter.validateAndWrite(diaryEntry, message?.channelId);
            } catch (error) {
                const message = `Error on diaryEntryWriter::validateAndWrite for '${diaryEntry?.filmTitle}' by '${diaryEntry?.userName}'`;
                container.resolve('logger').error(message, { error });
            }

            // Exit early if nothing returned from validate and write.
            // This shard may not have access to the channel needed for the user so this is
            // a normal and expected behavior and no logs are neccessary here.
            if (!userModel) {
                return;
            }

            // If the returning diary entry has available publishing data log it
            if (message?.entry?.updatedDate && message?.entry?.publishSource) {
                container.resolve('logger').info('Entry Publish Delay', {
                    delay: Date.now() - message.entry.updatedDate,
                    source: message.entry.publishSource,
                });
            }

            // Write to the database
            return container.resolve('firestorePreviousDao').update(userModel, diaryEntry);
        }
        setInterval(processOneMessageListEntry, 100, this.container);

        // Listen for LogEntry PubSub messages posted and respond
        // Create the function and use it. Normally I'd use arrow functions but trying to track down a memory issue.
        function messageProcessor(message: any): void {
            // Acknowledge the message immediatly to remove it from the queue
            // They systems will continue to seamlessly retry if the system fails at this point
            message.ack();
            // Push the JSON object to the message list
            logEntryMessageList.push(JSON.parse(message.data.toString()));
            return;
        }
        this.container.resolve('pubSubMessageListener').onLogEntryMessage(messageProcessor);
    }

    cleanUp() {
        // Ensure all the PubSub connections are closed
        this.container.resolve('pubSubConnection').closeAllSubscriptions();

        // Close the Discord connection and exit the process
        this.container
            .resolve('discordConnection')
            .getConnectedClient()
            .then((client: any) => {
                client.destroy();
                // Force a process exit due to how the discord-hybrid-sharding library works
                process.exit();
            });
    }
}

import config from '../../config.js';
import container from '../../dependency-injection-container.js';
container(config)
    .initialize()
    .then((awilixContainer: any) => {
        new Single(awilixContainer).run();
    });
