#!/usr/bin/env node

import config from '../config.mjs';
import container from '../dependency-injection-container.mjs';

// Allow production override
if (process.argv[2] == 'prod') {
    config.loadFile('./config/production.json');
    config.set('googleCloudIdentityKeyFile', '.gcp-key.json');
}

// Object containing all Application Commands
const commands = [
    {
        name: 'help',
        description: 'Replies with a some helpful information and links.',
        options: [
            {
                name: 'test',
                description: 'Sends a series of test messages to validate permissions',
                type: 5,
                required: false,
            },
        ],
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

// Initialize container
const initializedContainer = await container(config).initialize();

// Get application keys
const discordApplicationId = config.get('discordApplicationId');
const discordTokenName = config.get('discordBotTokenName');
const discordToken = await initializedContainer.resolve('secretManager').getValue(discordTokenName);

// Setup API call
const discordLibrary = initializedContainer.resolve('discordLibrary');
const rest = new discordLibrary.REST().setToken(discordToken);
const commandRoute = discordLibrary.Routes.applicationCommands(discordApplicationId);

// ...and go!
rest.put(commandRoute, { body: commands })
    .then((commandList) => {
        console.log(`✅  Set ${commandList.length} Application Commands`);
    })
    .catch((e) => {
        console.log('❌ Unable to remove Application Commands');
        console.log(e);
    });
