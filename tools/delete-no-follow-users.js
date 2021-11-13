#!/usr/bin/env node

const ConfigFactory = require('../factories/config-factory');
const DependencyInjectionContainer = require('../dependency-injection-container');
const dotenv = require('dotenv');
const fs = require('fs');

// Load .env into process.env, create config, create container
dotenv.config();
const configModel = new ConfigFactory('prod', process.env, {}, fs.existsSync).build();
const container = new DependencyInjectionContainer(configModel);

const collection = container.resolve('firestoreConnection').getCollection();
const data = collection.where('channelList', '==', []);
processData(data);

async function processData(collection) {
    const fileName = 'No Follow Deletes ' + new Date().toString() + '.txt';

    const querySnapshot = await collection.get();
    for (const key in querySnapshot.docs) {
        const documentSnapshot = querySnapshot.docs[key];
        const userData = documentSnapshot.data();

        if (userData.channelList.length === 0) {
            const logData = { id: documentSnapshot.id, userData };
            fs.writeFileSync(fileName, JSON.stringify(logData) + '\n', { flag: 'a+' });
            await documentSnapshot.ref.delete();
            console.log(`❌ Deleted ${documentSnapshot.id}`);
        }
    }
}
