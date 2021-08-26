#!/usr/bin/env node

const ConfigFactory = require('../factories/config-factory');
const DependencyInjectionContainer = require('../dependency-injection-container');
const dotenv = require('dotenv');
const fs = require('fs');

// Load .env into process.env, create config, create container
dotenv.config();
const configModel = new ConfigFactory('prod', process.env, {}, fs.existsSync).build();
const container = new DependencyInjectionContainer(configModel);

const commands = [
    {
        name: 'help',
        description: 'Replies with a some helpful information and links.',
    },
    {
        name: 'follow',
        description: 'Adds the Letterboxd account to the following list this channel.',
        options: [
            {
                name: 'account',
                description: 'Letterboxd account name',
                type: 3,
                required: true,
            },
        ],
    },
    {
        name: 'unfollow',
        description: 'Removes the Letterboxd account from the following list in this channel.',
        options: [
            {
                name: 'account',
                description: 'Letterboxd account name',
                type: 3,
                required: true,
            },
        ],
    },
    {
        name: 'following',
        description: 'Replies with a list of all accounts followed in this channel.',
    },
    {
        name: 'refresh',
        description: 'Updates the Filmlinkd cache for the Letterboxd account.',
        options: [
            {
                name: 'account',
                description: 'Letterboxd account name',
                type: 3,
                required: true,
            },
        ],
    },
];

// Update global commands for eventual distribution to all guilds
const discordRest = container.resolve('discordRest');
const rest = new discordRest({ version: '9' }).setToken(configModel.discordBotToken);
const commandRoute = container
    .resolve('discordRoutes')
    .applicationCommands(configModel.discordClientId);
rest.put(commandRoute, { body: commands })
    .then((/** @type {*[]} */ commandList) => {
        console.log(`Set ${commandList.length} global Application Commands`);
    })
    .catch(() => {
        console.log('Unable to set global Application Commands');
    });
