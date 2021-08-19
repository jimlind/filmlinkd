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

container
    .resolve('discordConnection')
    .getConnectedClient()
    .then((discordClient) => {
        /*
        const serverCount = discordClient.guilds.cache.size;
        container.resolve('logger').info(`Discord Client Logged In on ${serverCount} Servers`);

        // Listen for discord messages posted and respond
        container.resolve('discordMessageListener').onMessage((message) => {
            container.resolve('actionTranslator').translate(message);
        });

        const interval = setInterval(() => {
            console.log('pip');
        }, 10 * 1000); // Every 10 seconds
        */
        // Clean up Discord processes when told to end
        death(() => {
            //clearInterval(interval);
            discordClient.destroy();
        });
    });

/*
container
    .resolve('pubSubConnection')
    .getSubscription()
    .then((pubSubSubscription) => {
        // Listen for PubSub messages posted and respond
        container.resolve('pubSubMessageListener').onMessage((message) => {
            const returnData = JSON.parse(message.data.toString());
            const diaryEntry = Object.assign(new DiaryEntry(), returnData?.entry);
            const channelIdList = returnData?.channelIdList || [];
            container
                .resolve('diaryEntryWriter')
                .validateAndWrite(diaryEntry, channelIdList, Boolean(channelIdList.length))
                .then(() => {
                    message.ack();
                });
        });

        // This is the part that posts RSS updates at a regular interval
        // Keeps track of if an active diary entry thread is running
        let threadRunning = false;
        let interval = null;

        // Get a random index from the user list
        container
            .resolve('subscribedUserList')
            .getRandomIndex()
            .then((index) => {
                const diaryRestInterval = 30 * 1000; // Give it 30 seconds to rest
                interval = setInterval(() => {
                    if (threadRunning) return;

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

        // Clean up PubSub processes when told to end
        death(() => {
            clearInterval(interval);
            pubSubSubscription.close();
        });
    });
    */

// Log the process death
death((signal, error) => {
    container.resolve('logger').info('Program Terminating', { signal, error });
});
