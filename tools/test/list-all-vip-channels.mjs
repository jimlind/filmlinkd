#!/usr/bin/env node

import { URL } from 'url';
import config from '../../config.mjs';
import container from '../../dependency-injection-container.mjs';

// Configure as production
const dir = new URL('.', import.meta.url).pathname;
config.loadFile(dir + '../../config/production.json');
config.set('googleCloudIdentityKeyFile', dir + '../../.gcp-key.json');

// Initialize container
const initializedContainer = await container(config).initialize();

// ...and go!
const dao = initializedContainer.resolve('firestoreVipDao');
processData(dao);

async function processData(dao) {
    const data = await dao.read();
    console.log(data);
}
