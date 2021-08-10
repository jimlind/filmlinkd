#!/usr/bin/env node
'use strict';

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
