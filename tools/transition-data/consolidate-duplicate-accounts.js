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
    const fileName = 'Duplicate User Checks ' + new Date().toString() + '.txt';
    const querySnapshot = await data.get();

    // Build userName to id list
    const actualUserRefList = {};
    const fullDocumentList = {};
    for (const key in querySnapshot.docs) {
        const documentSnapshot = querySnapshot.docs[key];
        const data = documentSnapshot.data();
        const userName = data.userName.toLowerCase();
        if (!actualUserRefList[userName]) {
            actualUserRefList[userName] = [];
        }
        actualUserRefList[userName].push(documentSnapshot.id);
        fullDocumentList[documentSnapshot.id] = data;
    }

    // Create documents with the lowercase document id
    fs.writeFileSync(fileName, '11111111111111111111' + '\n', { flag: 'a+' });
    for (const userName in actualUserRefList) {
        const documentIdList = actualUserRefList[userName];
        const index = documentIdList.indexOf(userName);
        // No document with lowercase id available
        if (index < 0) {
            const documentId = documentIdList[0];
            const documentData = fullDocumentList[documentId];
            fs.writeFileSync(fileName, JSON.stringify(documentData) + '\n', { flag: 'a+' });

            documentData.channelList = [];
            const documentReference = collection.doc(userName);
            await documentReference.set(documentData);
            console.log(`✅ Created ${userName} from ${documentId}`);
        }
    }

    const actualDuplicateList = {};
    // Remove userNames that aren't duplicates
    for (const userName in actualUserRefList) {
        const documentIdList = actualUserRefList[userName];
        if (documentIdList.length > 1) {
            actualDuplicateList[userName] = documentIdList;
        }
    }

    // Copy most recent previous data to all duplicates
    fs.writeFileSync(fileName, '22222222222222222222' + '\n', { flag: 'a+' });
    for (const userName in actualDuplicateList) {
        const documentIdList = actualUserRefList[userName];
        const actualPreviousList = [];
        for (const key in documentIdList) {
            const documentId = documentIdList[key];
            actualPreviousList.push(fullDocumentList[documentId].previous);
        }
        const mostRecentPrevious = actualPreviousList.reduce((prev, current) => {
            if (current?.published || 0 > prev?.published || 0) {
                return current;
            } else {
                return prev;
            }
        });

        for (const key in documentIdList) {
            const documentId = documentIdList[key];
            const keyPublished = fullDocumentList[documentId].previous?.published;
            if (keyPublished !== mostRecentPrevious?.published && mostRecentPrevious?.published) {
                const documentReference = await collection.doc(documentId).get();
                const documentData = documentReference.data();
                fs.writeFileSync(fileName, JSON.stringify(documentData) + '\n', { flag: 'a+' });
                documentData.previous = mostRecentPrevious;
                await documentReference.ref.update(documentData);
                console.log(`✅ Updated previous data for ${documentId}`);
            }
        }
    }

    // Consolidate channel list to the lower case document id
    fs.writeFileSync(fileName, '33333333333333333333' + '\n', { flag: 'a+' });
    for (const userName in actualDuplicateList) {
        const documentIdList = actualUserRefList[userName];
        const actualChannelList = [];
        for (const key in documentIdList) {
            const documentId = documentIdList[key];
            const channelList = fullDocumentList[documentId].channelList;
            actualChannelList.push(...channelList);
        }
        const cleanChannelList = [
            ...new Map(actualChannelList.map((item) => [item['channelId'], item])).values(),
        ];

        for (const key in documentIdList) {
            const documentId = documentIdList[key];
            const channelList = fullDocumentList[documentId].channelList;
            // Skipping data clearing if already empty
            if (channelList.length == 0) {
                continue;
            }

            // Skipping data clearing if it'll get caught in the next step
            if (documentId === userName) {
                continue;
            }

            // Log existing data and update with an empty channelList
            const documentReference = await collection.doc(documentId).get();
            const documentData = documentReference.data();
            fs.writeFileSync(fileName, JSON.stringify(documentData) + '\n', { flag: 'a+' });
            documentData.channelList = [];
            await documentReference.ref.update(documentData);
            console.log(`🚮 Deleted channel list for document ${documentId}`);
        }

        if (cleanChannelList.length !== fullDocumentList[userName].channelList.length) {
            const documentReference = await collection.doc(userName).get();
            const documentData = documentReference.data();
            fs.writeFileSync(fileName, JSON.stringify(documentData) + '\n', { flag: 'a+' });
            documentData.channelList = cleanChannelList;
            await documentReference.ref.update(documentData);
            console.log(`✅ Updated channel list for ${userName}`);
        }
    }
}
