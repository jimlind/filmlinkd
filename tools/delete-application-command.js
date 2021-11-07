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
const commandRoute = container
    .resolve('discordRoutes')
    .applicationCommands(configModel.discordClientId);

rest.put(commandRoute, { body: [] })
    .then((/** @type {*[]} */ commandList) => {
        console.log(`Set ${commandList.length} global Application Commands`);
    })
    .catch(() => {
        console.log('Unable to set global Application Commands');
    });
