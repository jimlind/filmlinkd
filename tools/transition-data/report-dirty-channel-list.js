#!/usr/bin/env node

const ConfigFactory = require('../../factories/config-factory');
const DependencyInjectionContainer = require('../../dependency-injection-container');
const dotenv = require('dotenv');
const fs = require('fs');

// Load .env into process.env, create config, create container
dotenv.config();
const configModel = new ConfigFactory('prod', process.env, {}, fs.existsSync).build();
const container = new DependencyInjectionContainer(configModel);
const collection = container.resolve('firestoreConnection').getCollection();
processData(collection);

async function processData(collection) {
    const querySnapshot = await collection.get();
    for (const key in querySnapshot.docs) {
        const documentSnapshot = querySnapshot.docs[key];
        const data = documentSnapshot.data();
        data.channelList.forEach((channel) => {
            const keys = Object.keys(channel);
            if (JSON.stringify(keys) !== '["channelId"]') {
                console.log(`❌ Looks Dirty`);
                console.log(data);
            }
        });
    }
}
