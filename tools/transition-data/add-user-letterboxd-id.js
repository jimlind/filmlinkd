#!/usr/bin/env node

const ConfigFactory = require('../../factories/config-factory');
const DependencyInjectionContainer = require('../../dependency-injection-container');
const dotenv = require('dotenv');
const fs = require('fs');

// Load .env into process.env, create config, create container
dotenv.config();
const configModel = new ConfigFactory('dev', process.env, {}, fs.existsSync).build();
const container = new DependencyInjectionContainer(configModel);
const collection = container.resolve('firestoreConnection').getCollection();

//const data = collection;
const data = collection.where('letterboxdId', '==', '');
processData(data);

async function processData(data) {
    const querySnapshot = await data.get();

    for (const key in querySnapshot.docs) {
        const documentSnapshot = querySnapshot.docs[key];
        const data = documentSnapshot.data();
        if (data.letterboxdId) {
            console.log(`👉 Skipping ${data.userName} (${data.letterboxdId})`);
            continue;
        }

        try {
            data.letterboxdId = await container
                .resolve('letterboxdLetterboxdIdWeb')
                .get(data.userName);
        } catch (error) {
            console.log(`🚫 Failed ${data.userName}`);
            data.letterboxdId = '';
            await documentSnapshot.ref.update(data);
            continue;
        }

        if (!data.letterboxdId) {
            console.log(`🚫 Empty ${data.userName}`);
            data.letterboxdId = '';
            await documentSnapshot.ref.update(data);
            continue;
        }

        await documentSnapshot.ref.update(data);
        console.log(`✅ Updated ${data.userName} (${data.letterboxdId})`);
    }
}
