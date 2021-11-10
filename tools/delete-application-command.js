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

// Update global commands for eventual distribution to all guilds
const discordRest = container.resolve('discordRest');
const rest = new discordRest({ version: '9' }).setToken(configModel.discordBotToken);
const discordRoutes = container.resolve('discordRoutes');

let commandRoute = discordRoutes.applicationCommands(configModel.discordClientId);
if (env === 'dev') {
    const guildId = '795053930283139073';
    commandRoute = discordRoutes.applicationGuildCommands(configModel.discordClientId, guildId);
}

rest.put(commandRoute, { body: [] })
    .then((/** @type {*[]} */ commandList) => {
        console.log(`✅ Set ${commandList.length} Application Commands`);
    })
    .catch(() => {
        console.log('❌ Unable to set Application Commands');
    });
