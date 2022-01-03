#!/usr/bin/env node

const ConfigFactory = require('../factories/config-factory');
const DependencyInjectionContainer = require('../dependency-injection-container');
const dotenv = require('dotenv');
const fs = require('fs');

// Load .env into process.env, create config, create container
dotenv.config();

const env = process.argv[2] || 'dev';
const configModel = new ConfigFactory(env, process.env, {}, fs.existsSync).build();
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
            {
                name: 'channel',
                description: 'Discord channel name',
                type: 3,
                required: false,
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
            {
                name: 'channel',
                description: 'Discord channel name',
                type: 3,
                required: false,
            },
        ],
    },
    {
        name: 'following',
        description: 'Replies with a list of all accounts followed in this channel.',
        options: [
            {
                name: 'channel',
                description: 'Discord channel name',
                type: 3,
                required: false,
            },
        ],
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
    {
        name: 'contributor',
        description: "Shows a film contributor's information snippet.",
        options: [
            {
                name: 'contributor-name',
                description: 'Contributor name',
                type: 3,
                required: true,
            },
        ],
    },
    {
        name: 'diary',
        description: 'Shows the 5 most recent diary entries.',
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
        name: 'film',
        description: "Shows a film's information snippet.",
        options: [
            {
                name: 'film-name',
                description: 'Film name',
                type: 3,
                required: true,
            },
        ],
    },
    {
        name: 'list',
        description: "Shows a members's list's information snippet.",
        options: [
            {
                name: 'account',
                description: 'Letterboxd account name',
                type: 3,
                required: true,
            },
            {
                name: 'list-name',
                description: "Member's list name",
                type: 3,
                required: true,
            },
        ],
    },
    {
        name: 'user',
        description: "Shows a users's information snippet.",
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
const discordRoutes = container.resolve('discordRoutes');

let commandRoute = discordRoutes.applicationCommands(configModel.discordClientId);
if (env === 'dev') {
    const guildId = '795053930283139073';
    commandRoute = discordRoutes.applicationGuildCommands(configModel.discordClientId, guildId);
}

rest.put(commandRoute, { body: commands })
    .then((/** @type {*[]} */ commandList) => {
        console.log(`✅  Set ${commandList.length} Application Commands`);
    })
    .catch(() => {
        console.log('❌ Unable to set Application Commands');
    });
