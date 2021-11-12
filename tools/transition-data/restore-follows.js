#!/usr/bin/env node
'use strict';

const ConfigFactory = require('../../factories/config-factory');
const DependencyInjectionContainer = require('../../dependency-injection-container');
const dotenv = require('dotenv');
const fs = require('fs');
const readline = require('readline');

// Load .env into process.env, create config, create container
dotenv.config();
const configModel = new ConfigFactory('prod', process.env, {}, fs.existsSync).build();
const container = new DependencyInjectionContainer(configModel);
const collection = container.resolve('firestoreConnection').getCollection();

processData(collection);

async function processData(data) {
    const fileName = 'Restoration Before State ' + new Date().toString() + '.txt';
    const fileStream = fs.createReadStream('./tools/transition-data/log/log004.txt');
    const read = readline.createInterface({ input: fileStream, crlfDelay: Infinity });
    const actualFollowList = {};
    for await (const line of read) {
        const obj = JSON.parse(line);
        const userName = obj.userName;

        if (!actualFollowList[userName]) {
            actualFollowList[userName] = [];
        }
        actualFollowList[userName].push(...obj.channelList);
    }

    for (const property in actualFollowList) {
        const data = collection.where('userName', '==', property).limit(1);
        const querySnapshot = await data.get();
        const documentSnapshot = querySnapshot.docs[0];
        const userData = documentSnapshot.data();
        fs.writeFileSync(fileName, JSON.stringify(userData) + '\n', { flag: 'a+' });

        const channelList = userData.channelList.concat(actualFollowList[property]);
        userData.channelList = [
            ...new Map(channelList.map((item) => [item['channelId'], item])).values(),
        ];

        await documentSnapshot.ref.update(userData);
        console.log(`✅  Updated ${userData.userName}`);
    }
}
