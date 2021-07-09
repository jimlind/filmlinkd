#!/usr/bin/env node

const ConfigFactory = require('./factories/config-factory');
const DependencyInjectionContainer = require('./dependency-injection-container');
const death = require('death');
const dotenv = require('dotenv');
const fs = require('fs');
const packageJson = require('./package.json');

// Load .env into process.env, create config, create container
dotenv.config();
const configModel = new ConfigFactory(
    process.argv[2],
    process.env,
    packageJson,
    fs.existsSync,
).build();
const container = new DependencyInjectionContainer(configModel);

container
    .resolve('discordConnection')
    .getConnectedClient()
    .then((discordClient) => {
        const serverCount = discordClient.guilds.cache.size;
        container.resolve('logger').info(`Discord Client Logged In on ${serverCount} Servers`);

        // Listen for discord messages posted and respond
        container.resolve('discordMessageListener').onMessage((message) => {
            container.resolve('actionTranslator').translate(message);
        });

        // Listen for PubSub messages posted and respond
        container.resolve('pubSubMessageListener').onMessage((message) => {
            // TODO:
            // This shouldn't actually be a "write entry" action.
            // This should be a "here is a potential new entry" action.
            container.resolve('diaryEntryWriter').write(message);
        });

        // This is the part that posts RSS updates at a regular interval
        // Keeps track of if an active diary entry thread is running
        let threadRunning = false;
        let interval = 0;

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
                        .resolve('diaryEntryPublisher')
                        .postPageOfEntries(index, config.pageSize)
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
            container.resolve('pubSubConnection').getSubscription().close();
            container.resolve('logger').info('Program Terminated', { signal, error });
        });
    });
