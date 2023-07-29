#!/usr/bin/env node

import { stdin as input, stdout as output } from 'node:process';
import * as readline from 'node:readline/promises';
import { URL } from 'url';
import config from '../../config.mjs';
import container from '../../dependency-injection-container.mjs';

// Setup input
const rl = readline.createInterface({ input, output });
const userName = await rl.question('Username of account to update footer? ');
const footerText = await rl.question('Text for the footer? ');
const footerImage = await rl.question('Image for the footer? ');
rl.close();

// Configure as production
const dir = new URL('.', import.meta.url).pathname;
config.loadFile(dir + '../../config/production.json');
config.set('googleCloudIdentityKeyFile', dir + '../../.gcp-key.json');

// Initialize container
const initializedContainer = await container(config).initialize();

// ...and go!
const collection = await initializedContainer.resolve('firestoreConnection').getCollection();
processData(collection, userName, footerText, footerImage);

async function processData(collection, userName, footerText, footerImage) {
    const query = collection.where('userName', '==', userName).limit(1);
    const querySnapshot = await query.get();
    const documentSnapshot = querySnapshot?.docs?.[0];

    if (!documentSnapshot) {
        return;
    }

    // Update the footer
    documentSnapshot.ref.update({ footer: { icon: footerImage, text: footerText } });
}
