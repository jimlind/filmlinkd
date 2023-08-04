#!/usr/bin/env node

import { stdin as input, stdout as output } from 'node:process';
import * as readline from 'node:readline/promises';
import { URL } from 'url';
import config from '../../config.mjs';
import container from '../../dependency-injection-container.mjs';

// Setup input
const rl = readline.createInterface({ input, output });
const channelId = await rl.question('Channel ID to lookup? ');
rl.close();

// Configure as production
const dir = new URL('.', import.meta.url).pathname;
config.loadFile(dir + '../../config/production.json');
config.set('googleCloudIdentityKeyFile', dir + '../../.gcp-key.json');

// Initialize container
const initializedContainer = await container(config).initialize();

// ...and go!
const dao = initializedContainer.resolve('firestoreSubscriptionDao');
processData(dao);

async function processData(dao) {
    const userList = await dao.list(channelId);
    console.log({ userList });
}
