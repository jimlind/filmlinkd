class Shard {
    constructor(container) {
        this.container = container;

        // Trigger clean up on process exit
        this.container.resolve('exitHook')(this.cleanUp.bind(this));
    }

    async run() {
        // Setup the connected client, may take a bit for the client to connect.
        const connection = this.container.resolve('discordConnection');
        const client = await connection.getConnectedClient();

        // Log a message that client has connected
        const message = `Discord Client Logged In on ${client.guilds.cache.size} Servers`;
        this.container.resolve('logger').info(message);

        // Start listening for internal and external events
        this.startInteractionListener();
        this.startPubSubMessageListener();
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
                .then((userModel) => {
                    // Exit early if nothing returned from validate and write.
                    // This shard may not have access to the channel needed for the user so this is
                    // a normal and expected behavior and no logs are neccessary here.
                    if (!userModel) {
                        return Promise.all([]);
                    }

                    // If the returning diary entry has available publishing data log it
                    if (returnData?.entry?.updatedDate && returnData?.entry?.publishSource) {
                        this.container.resolve('logger').info('Entry Publish Delay', {
                            delay: Date.now() - returnData.entry.updatedDate,
                            source: returnData.entry.publishSource,
                        });
                    }

                    // Trial and error logging. Not sure what's going on yet.
                    if (!userModel.letterboxdId) {
                        const state = { userModel, diaryEntry };
                        this.container
                            .resolve('logger')
                            .warn('ISS3.1: User model letterboxdId is falsey', state);
                    }

                    // Write to the database
                    return this.container
                        .resolve('firestorePreviousDao')
                        .update(userModel, diaryEntry);
                })
                .catch((e) => {
                    const message = `Error on diaryEntryWriter::validateAndWrite for '${diaryEntry?.filmTitle}' by '${diaryEntry?.userName}'`;
                    this.container.resolve('logger').error(message, { error: e });
                });
        });
    }

    cleanUp() {
        // Close the Discord client's connection
        this.container.resolve('discordClient').destroy();

        // Ensure all the PubSub connections are closed
        this.container.resolve('pubSubConnection').closeAllSubscriptions();
    }
}

// This creates a nwe instance of the dependency injection container for every shard.
// That feels a bit excessive, but allows each shard to exist independently which is how shards work.
import config from '../../config.mjs';
import container from '../../dependency-injection-container.mjs';
const initializedContainer = await container(config).initialize();
new Shard(initializedContainer).run();
