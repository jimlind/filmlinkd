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
const httpClient = container.resolve('httpClient');

processData(collection);

async function processData(collection) {
    const querySnapshot = await collection.get();

    for (const key in querySnapshot.docs) {
        const documentSnapshot = querySnapshot.docs[key];
        const data = documentSnapshot.data();

        if (!data?.previous?.uri) {
            console.log(`❌ ${data.userName} has incomplete previous data`);
            continue;
        }

        const entryLid = await getEntryLid(data.previous.uri);
        if (!entryLid) {
            console.log(`❌ ${data.userName} error occured getting data`);
        }

        data.previous = {
            ...data.previous,
            lid: entryLid,
        };
        await documentSnapshot.ref.update(data);
        console.log(`✅ Updated ${data.previous.uri} (${entryLid})`);

        // console.log({ uri: data.previous.uri, entryLid });

        // console.log(`${data.userName} has incomplete previous data`);
        // let diaryEntryList = [];
        // try {
        //     diaryEntryList = await container.resolve('letterboxdDiary').get(data.userName, 1);
        // } catch {
        //     console.log(`--- Failure on ${data.userName}`);
        // }
        // if (diaryEntryList.length) {
        //     const diaryEntry = diaryEntryList[0];
        //     data.previous = {
        //         id: diaryEntry.id,
        //         published: diaryEntry.publishedDate,
        //         uri: diaryEntry.link,
        //     };
        //     await documentSnapshot.ref.update(data);
        //     console.log(`--- Updated ${data.userName}`);
        // }
    }
}

async function getEntryLid(entryUri) {
    return httpClient
        .head(entryUri, 10000)
        .then((response) => {
            const letterboxdId = response?.headers['x-letterboxd-identifier'] || '';
            if (!letterboxdId) {
                console.log(`❌ ${entryUri} doesn't have a Letterboxd Id`);
                return '';
            }
            return letterboxdId;
        })
        .catch(() => {
            console.log(`❌ ${entryUri} is a bad URL`);
            return '';
        });
}
