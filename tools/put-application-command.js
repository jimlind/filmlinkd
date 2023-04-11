#!/usr/bin/env node

const { REST, Routes } = require('discord.js');

process.env.npm_config_live = process.argv[2] == 'prod' || false;
const config = require('../config.js');
if (process.env.npm_config_live) {
    config.set('googleCloudIdentityKeyFile', './.gcp-key.json');
}

const container = require('../dependency-injection-container')(config);
const commands = [
    {
        name: 'help',
        description: 'Replies with a some helpful information and links.',
    },
    {
        name: 'follow',
        description: 'Adds the Letterboxd account to the following list this channel.',
        default_member_permissions: 1 << 5,
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
        default_member_permissions: 1 << 5,
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
        name: 'logged',
        description: "Shows the user's 5 most recent logged entries for a film.",
        options: [
            {
                name: 'account',
                description: 'Letterboxd account name',
                type: 3,
                required: true,
            },
            {
                name: 'film-name',
                description: 'Film name',
                type: 3,
                required: true,
            },
        ],
    },
    {
        name: 'roulette',
        description: 'Displays random film information snippet.',
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

// Update global commands
processCommands(commands);

async function processCommands(commands) {
    const config = container.resolve('config');
    const discordApplicationId = config.get('discordApplicationId');
    const discordTokenName = config.get('discordBotTokenName');
    const discordToken = await container.resolve('secretManager').getValue(discordTokenName);
    const rest = new REST({ version: '10' }).setToken(discordToken);

    let commandRoute = Routes.applicationCommands(discordApplicationId);
    if (process.env.npm_config_live === false) {
        const guildId = 's795053930283139073';
        commandRoute = Routes.applicationGuildCommands(discordApplicationId, guildId);
    }

    rest.put(commandRoute, { body: commands })
        .then((commandList) => {
            console.log(`✅  Set ${commandList.length} Application Commands`);
        })
        .catch(() => {
            console.log('❌ Unable to set Application Commands');
        });
}
