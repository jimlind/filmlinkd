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

processData(collection);

async function processData(data) {
    const querySnapshot = await data.get();

    for (const key in querySnapshot.docs) {
        const documentSnapshot = querySnapshot.docs[key];
        const data = documentSnapshot.data();
        const originalUserName = data.userName;
        const lowercaseUserName = data.userName.toLowerCase();
        if (originalUserName !== lowercaseUserName) {
            data.userName = lowercaseUserName;
            await documentSnapshot.ref.update(data);
            console.log(`✅ Updated ${originalUserName} to ${lowercaseUserName}`);
            continue;
        }
    }
}
