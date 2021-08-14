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
processData(collection);

async function processData(collection) {
    const querySnapshot = await collection.get();
    var documentList = querySnapshot.docs;
    const fileName = 'Bad User Deletes ' + new Date().toString() + '.txt';

    for (var i = 0; i < 10; i++) {
        console.log(`${documentList.length} users to check...`);
        for (const key in documentList) {
            const data = documentList[key].data();
            try {
                await container.resolve('letterboxdDiaryRss').get(data.userName, 1);
                console.log(`+++ Success on ${data.userName}`);
                documentList[key] = false;
            } catch (error) {
                console.log(`--- Failure on ${data.userName}`);
            }
        }
        documentList = documentList.filter((x) => !!x);
    }

    for (const key in documentList) {
        const document = documentList[key];
        const data = document.data();

        const content = data.userName + '||' + JSON.stringify(data.channelList) + '\n';
        fs.writeFileSync(fileName, content, { flag: 'a+' });
        await document.ref.delete();
    }
}
