#!/usr/bin/env node

// Use production data but use the key so it can be accessed remotely
process.env.npm_config_live = true;
const config = require('../../config.js');
config.set('gcpKeyFile', './.gcp-key.json');

// ...and go!
const container = require('../../dependency-injection-container')(config);
const collection = container.resolve('firestoreConnection').getCollection();
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
