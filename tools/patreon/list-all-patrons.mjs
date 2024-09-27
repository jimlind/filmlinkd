#!/usr/bin/env node

import { URL } from 'url';
import config from '../../dist/config.js';
import container from '../../dist/dependency-injection-container.js';

// Configure as production
const dir = new URL('.', import.meta.url).pathname;
config.loadFile(dir + '../../config/production.json');
config.set('googleCloudIdentityKeyFile', dir + '../../.gcp-key.json');

// Initialize container
const initializedContainer = await container(config).initialize();

// ...and go!
const collection = initializedContainer.resolve('firestoreConnection').getCollection();
processData(collection);

async function processData(collection) {
    const query = collection.where('footer', '!=', null);
    const querySnapshot = await query.get();

    const data = querySnapshot.docs.map((documentSnapshot) => {
        const userData = documentSnapshot.data();
        return {
            userName: userData.userName,
            title: userData.footer.text,
            image: userData.footer.icon,
        };
    });
    data.sort((a, b) => (a.userName > b.userName ? 1 : -1));
    console.table(data);
}
