#!/usr/bin/env node

import config from '../config.mjs';
import container from '../dependency-injection-container.mjs';

// Allow production override
if (process.argv[2] == 'prod') {
    config.loadFile('./config/production.json');
    config.set('googleCloudIdentityKeyFile', '.gcp-key.json');
}

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
rest.put(commandRoute, { body: [] })
    .then((commandList) => {
        if (commandList.length > 0) {
            throw 'More than zero commands returned';
        }
        console.log(`✅  Removed Application Commands`);
    })
    .catch((e) => {
        console.log('❌ Unable to remove Application Commands');
        console.log(e);
    });
