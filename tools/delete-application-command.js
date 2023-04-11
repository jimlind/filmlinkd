#!/usr/bin/env node

const { REST, Routes } = require('discord.js');

process.env.npm_config_live = process.argv[2] == 'prod' || false;
const config = require('../config.js');
if (process.env.npm_config_live) {
    config.set('googleCloudIdentityKeyFile', './.gcp-key.json');
}

const container = require('../dependency-injection-container')(config);
processCommands();

async function processCommands() {
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

    rest.put(commandRoute, { body: [] })
        .then((commandList) => {
            console.log(`✅  Set ${commandList.length} Application Commands`);
        })
        .catch(() => {
            console.log('❌ Unable to set Application Commands');
        });
}
