#!/usr/bin/env node

const ConfigFactory = require('../factories/config-factory');
const DependencyInjectionContainer = require('../dependency-injection-container');
const dotenv = require('dotenv');
const fs = require('fs');

// Load .env into process.env, create config, create container
dotenv.config();
const configModel = new ConfigFactory('dev', process.env, [], fs.existsSync).build();
const container = new DependencyInjectionContainer(configModel);

// Configs for the messages posted
const userName = 'yoli_zina';
const internalTestingChannelId = '799785154032959528';

const mockUser = {
    displayName: userName,
    userName: userName,
    image: 'https://placedog.net/100',
    channelList: [{ channelId: internalTestingChannelId }],
};

container
    .resolve('discordConnection')
    .getConnectedClient()
    .then((discordClient) => {
        const diaryEntryWriter = container.resolve('diaryEntryWriter');
        const serverCount = discordClient.guilds.cache.size;
        container.resolve('logger').info(`Discord Client Logged In on ${serverCount} Servers`);

        container
            .resolve('diaryEntryProcessor')
            .getNewEntriesForUser({ userName, previousId: 0 }, 2)
            .then((entryList) => {
                const promiseList = entryList
                    .map((entry) => diaryEntryWriter.createSenderPromiseList(entry, mockUser))
                    .flat();

                Promise.all(promiseList).finally(() => {
                    discordClient.destroy();
                });
            });
    });
