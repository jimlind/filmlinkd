#!/usr/bin/env node

const ConfigFactory = require('./factories/config-factory');
const DependencyInjectionContainer = require('./dependency-injection-container');
const DiaryEntry = require('./models/diary-entry');
const death = require('death');
const dotenv = require('dotenv');
const fs = require('fs');

// @ts-ignore TypeScript validation doesn't like json files as a module
const packageJson = require('./package.json');

// Load .env into process.env, create config, create container
dotenv.config();
const configModel = new ConfigFactory(
    process.argv[2],
    process.env,
    packageJson,
    fs.existsSync,
).build();

// Make sure the promises for PubSub and Discord Clients connect
const container = new DependencyInjectionContainer(configModel);
Promise.all([
    container.resolve('discordConnection').getConnectedClient(),
    container.resolve('pubSubConnection').getLogEntrySubscription(),
    container.resolve('pubSubConnection').getLogEntryResultSubscription(),
]).then(([discordClient, pubSubLogEntrySubscription, pubSubLogEntryResultSubscription]) => {
    const serverCount = discordClient.guilds.cache.size;
    container.resolve('logger').info(`Discord Client Logged In on ${serverCount} Servers`);

    // Listen for discord interactions and respond
    container.resolve('discordInteractionListener').onInteraction((commandInteraction) => {
        container.resolve('interactionTranslator').translate(commandInteraction);
    });

    // Listen for LogEntry PubSub messages posted and respond
    container.resolve('pubSubMessageListener').onLogEntryMessage((message) => {
        message.ack();
        const returnData = JSON.parse(message.data.toString());
        const diaryEntry = Object.assign(new DiaryEntry(), returnData?.entry);
        container
            .resolve('diaryEntryWriter')
            .validateAndWrite(diaryEntry, returnData?.channelId)
            .catch(() => {
                container
                    .resolve('logger')
                    .error(
                        `Entry for '${diaryEntry?.filmTitle}' by '${diaryEntry?.userName}' did not validate and write. [002]`,
                    );
            });
    });

    // This is the part that posts RSS updates at a regular interval
    // Keeps track of if an active diary entry thread is running
    let threadRunning = false;
    let interval = null;

    const isShardZero = (discordClient?.shard?.ids || [0]).includes(0);

    // Listen for LogEntryResult PubSub messages posted and respond
    container.resolve('pubSubMessageListener').onLogEntryResultMessage((message) => {
        message.ack();
        if (isShardZero) {
            const returnData = JSON.parse(message.data.toString());
            const upsertResult = container
                .resolve('subscribedUserList')
                .upsert(returnData.userName, returnData.userLid, returnData.previousId);

            if (upsertResult == returnData.previousId) {
                const diaryEntry = returnData.diaryEntry;
                container
                    .resolve('firestoreUserDao')
                    .getByUserName(returnData.userName)
                    .then((userModel) => {
                        container.resolve('firestorePreviousDao').update(userModel, diaryEntry);
                    });
            }
        }
    });

    if (isShardZero) {
        // Get a random index from the user list
        container
            .resolve('subscribedUserList')
            .getRandomIndex()
            .then((index) => {
                const diaryRestInterval = 30 * 1000; // Give it 30 seconds to rest
                interval = setInterval(() => {
                    if (threadRunning) return;

                    if (!discordClient.isReady()) {
                        container.resolve('logger').info('Client Not Ready Reset');
                        return process.exit();
                    }

                    const config = container.resolve('config');
                    container
                        .resolve('diaryEntryProcessor')
                        .processPageOfEntries(index, config.pageSize)
                        .then((pageCount) => {
                            threadRunning = false;
                            index = pageCount === 0 ? 0 : index + pageCount;
                        });

                    threadRunning = true;
                }, diaryRestInterval);
            });
    }

    // Log as much as I can about a failed promise
    // It might help me debug the wierd failure that happens
    process.on('unhandledRejection', (reason, promise) => {
        container.resolve('logger').error('Unhandled Rejection', {
            reasonType: typeof reason,
            reasonObject: reason,
            promiseType: typeof promise,
            promiseObject: promise,
            discordClientObject: discordClient,
            isShardZero,
        });
    });

    // Clean up when process is told to end
    death((signal, error) => {
        clearInterval(interval);
        discordClient.destroy();
        pubSubLogEntrySubscription.close();
        pubSubLogEntryResultSubscription.close();
        container.resolve('logger').info('Program Terminated', { signal, error });
    });
});
