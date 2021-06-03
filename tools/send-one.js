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
const quantity = 1;

const user = {
    userName: userName,
    previousId: 0,
};

container
    .resolve('discordConnection')
    .getConnectedClient()
    .then((discordClient) => {
        const serverCount = discordClient.guilds.cache.size;
        container.resolve('logger').info(`Discord Client Logged In on ${serverCount} Servers`);

        // This will write to all dev channels the user is followed in and quit
        container
            .resolve('diaryEntryWriter')
            .postEntriesForUser(user, quantity)
            .then(() => {
                discordClient.destroy();
            });
    });
