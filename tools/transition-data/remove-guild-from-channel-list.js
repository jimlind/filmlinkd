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
    const fileName = 'Channel Updates ' + new Date().toString() + '.txt';
    const querySnapshot = await collection.get();

    for (const key in querySnapshot.docs) {
        const documentSnapshot = querySnapshot.docs[key];
        const data = documentSnapshot.data();

        const content = data.userName + '||' + JSON.stringify(data.channelList) + '\n';
        fs.writeFileSync(fileName, content, { flag: 'a+' });

        data.channelList = data.channelList.map((element) => {
            return { channelId: element.channelId };
        });

        await documentSnapshot.ref.update(data);
        console.log(`--- Updated ${data.userName}`);
    }
}
