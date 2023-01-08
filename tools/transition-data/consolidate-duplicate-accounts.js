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
    const list = [];

    const querySnapshot = await data.get();
    for (const key in querySnapshot.docs) {
        const documentSnapshot = querySnapshot.docs[key];
        const data = documentSnapshot.data();
        const originalUserName = data.userName;
        list[key] = data.userName;
    }

    toFindDuplicates(list).forEach(async (username) => {
        const fileName = 'Duplicate User Deletes ' + new Date().toString() + '.txt';
        const indexes = list.reduce((prev, curr, index) => {
            if (curr === username) {
                prev.push(index);
                return prev;
            } else {
                return prev;
            }
        }, []);

        let mergedChannels = [];
        indexes.forEach(async (index) => {
            const documentSnapshot = querySnapshot.docs[index];
            const data = documentSnapshot.data();
            fs.writeFileSync(fileName, JSON.stringify(data) + '\n', { flag: 'a+' });

            mergedChannels = mergedChannels.concat(data.channelList);

            data.channelList = [];
            // This happens async and it screws things up
            // Don't use this willy nilly
            //await documentSnapshot.ref.update(data);
        });

        const firstDoc = querySnapshot.docs[indexes[0]];
        const firstData = firstDoc.data();
        firstData.channelList = mergedChannels;
        //await firstDoc.ref.update(firstData);
    });
}

const toFindDuplicates = (list) => list.filter((item, index) => list.indexOf(item) !== index);
