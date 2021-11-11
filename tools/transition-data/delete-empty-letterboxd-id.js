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
const query = collection.where('letterboxdId', '==', '');

processData(query);

async function processData(query) {
    const fileName = 'Empty User Deletes ' + new Date().toString() + '.txt';
    const querySnapshot = await query.get();
    querySnapshot.forEach(async (documentSnapshot) => {
        const userData = documentSnapshot.data();
        const userName = userData.userName;

        let letterboxdId = false;
        let letterboxdEntry = false;
        try {
            letterboxdId = await container.resolve('letterboxdLetterboxdIdWeb').get(userName);
            letterboxdEntry = await container.resolve('letterboxdDiaryRss').get(userName, 1);
        } catch (e) {}

        if (!letterboxdId && !letterboxdEntry) {
            fs.writeFileSync(fileName, JSON.stringify(userData) + '\n', { flag: 'a+' });
            await documentSnapshot.ref.delete();
            console.log(`❌ Deleted ${userName}`);
        } else {
            console.log(`✅ Kept ${userName}`);
        }
    });
}
