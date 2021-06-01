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
const userName = 'slim';
const quantity = 20;
const channelId = '799785154032959528'; // This is an intenal channel for testing
const mockUser = {
    displayName: userName,
    userName: userName,
    image: 'https://placedog.net/100',
};

container
    .resolve('discordConnection')
    .getConnectedClient()
    .then((discordClient) => {
        const serverCount = discordClient.guilds.cache.size;
        container.resolve('logger').info(`Discord Client Logged In on ${serverCount} Servers`);

        container
            .resolve('letterboxdDiaryRss')
            .get(userName, quantity)
            .then((entryList) => {
                entryList.forEach((entry, index) => {
                    setTimeout(() => {
                        const message = container
                            .resolve('messageEmbedFactory')
                            .createDiaryEntryMessage(entry, mockUser);

                        container
                            .resolve('discordMessageSender')
                            .send(channelId, message)
                            .then(() => {
                                if (entryList.length == index + 1) {
                                    discordClient.destroy();
                                }
                            });
                    }, index * 1000);
                });
            });
    });
