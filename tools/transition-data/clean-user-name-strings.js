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
const lidWebService = container.resolve('letterboxdLidWeb');
const memberAPI = container.resolve('letterboxdMemberApi');

processData(collection);

async function processData(collection) {
    const list = [];

    const querySnapshot = await collection.get();
    for (const key in querySnapshot.docs) {
        const documentSnapshot = querySnapshot.docs[key];
        const data = documentSnapshot.data();
        const storedName = data.userName;

        // Skip anything that is a super clean name
        if (/^[a-z0-9]+$/.test(storedName)) {
            continue;
        }

        let foundName = '';
        try {
            const foundLid = await lidWebService.get(storedName);
            const member = await memberAPI.getMember(foundLid);
            foundName = member.userName;
        } catch (error) {}

        if (!foundName) {
            console.log(`🟡 User ${storedName} not found`);
        } else if (storedName !== foundName) {
            console.log(`🔴 Mismatch of ${storedName} and ${foundName}`);

            const foundQuery = collection.where('userName', '==', foundName).limit(1);
            const foundQuerySnapshot = await foundQuery.get();
            const foundDocumentSnapshot = foundQuerySnapshot.docs[0];
            const foundUserData = foundDocumentSnapshot.data();

            foundUserData.channelList = foundUserData.channelList.concat(data.channelList);

            await foundDocumentSnapshot.ref.update(foundUserData);
            await documentSnapshot.ref.delete();
        }
    }
}
