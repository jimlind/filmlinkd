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
    container.resolve('pubSubConnection').getSubscription(),
]).then(([discordClient, pubSubSubscription]) => {
    const serverCount = discordClient.guilds.cache.size;
    container.resolve('logger').info(`Discord Client Logged In on ${serverCount} Servers`);

    // Listen for discord interactions and respond
    container.resolve('discordInteractionListener').onInteraction((commandInteraction) => {
        container.resolve('interactionTranslator').translate(commandInteraction);
    });

    // Listen for PubSub messages posted and respond
    container.resolve('pubSubMessageListener').onMessage((message) => {
        const returnData = JSON.parse(message.data.toString());
        const diaryEntry = Object.assign(new DiaryEntry(), returnData?.entry);
        container
            .resolve('diaryEntryWriter')
            .validateAndWrite(diaryEntry, returnData?.channelId)
            .then(() => {
                message.ack();
            })
            .catch((error) => {
                const data = { error, returnData };
                container
                    .resolve('logger')
                    .error(`Validate and write error for '${diaryEntry?.userName}'`, data);
            });
    });

    // This is the part that posts RSS updates at a regular interval
    // Keeps track of if an active diary entry thread is running
    // A nasty hack here to say the thread is running so it skips the logic on non-primary shards
    let threadRunning = !(discordClient?.shard?.ids || [0]).includes(0);
    let interval = null;

    // Get a random index from the user list
    container
        .resolve('subscribedUserList')
        .getRandomIndex()
        .then((index) => {
            const diaryRestInterval = 30 * 1000; // Give it 30 seconds to rest
            const startTime = Date.now();
            interval = setInterval(() => {
                if (threadRunning) return;

                if (Date.now() > startTime + 4 * 60 * 60000) {
                    container.resolve('logger').info('4 Hour Reset');
                    return process.exit();
                }

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

    // Clean up when process is told to end
    death((signal, error) => {
        clearInterval(interval);
        discordClient.destroy();
        pubSubSubscription.close();
        container.resolve('logger').info('Program Terminated', { signal, error });
    });
});
