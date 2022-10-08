#!/usr/bin/env node

const ConfigFactory = require('../../factories/config-factory');
const DependencyInjectionContainer = require('../../dependency-injection-container');
const dotenv = require('dotenv');
const fs = require('fs');

// Load .env into process.env, create config, create container
dotenv.config();
const configModel = new ConfigFactory('prod', process.env, {}, fs.existsSync).build();
const container = new DependencyInjectionContainer(configModel);
const collection = container.resolve('firestoreConnection').getCollection();
const lidWebService = container.resolve('letterboxdLidWeb');
const memberAPI = container.resolve('letterboxdMemberApi');

const deletesFileName = 'Duplicate LID Deletes ' + new Date().toString() + '.txt';
const editsFileName = 'Duplicate LID Edits ' + new Date().toString() + '.txt';
processData(collection);

async function processData(data) {
    const list = [];

    const querySnapshot = await data.get();
    for (const key in querySnapshot.docs) {
        const documentSnapshot = querySnapshot.docs[key];
        const data = documentSnapshot.data();

        list.push(data.letterboxdId);
    }
    list.sort();
    const duplicates = returnOnlyDuplicates(list);
    const uniqueDuplicates = [...new Set(duplicates)];

    console.log(`Found ${list.length} accounts.`);
    console.log(`Found ${uniqueDuplicates.length} duplicative accounts.`);

    uniqueDuplicates.forEach(async (letterboxdId) => {
        const query = collection.where('letterboxdId', '==', letterboxdId);
        const documentSnapshotList = await query.get();

        const sameUsers = [];
        for (const key in documentSnapshotList.docs) {
            const data = documentSnapshotList.docs[key].data();
            sameUsers.push(data);
        }
        await consolidateUsers(sameUsers);
    });
}

async function consolidateUsers(users) {
    let newUser = users.pop();
    let newFooter = newUser.footer;
    let channelList = newUser.channelList;
    let oldUserList = [];

    for (const key in users) {
        const user = users[key];
        channelList = channelList.concat(user.channelList);

        if (user.updated > newUser.updated) {
            oldUserList.push(newUser);
            newUser = user;
        } else {
            oldUserList.push(user);
        }

        if (user.footer) {
            newFooter = user.footer;
        }
    }
    if (newFooter) {
        newUser.footer = newFooter;
    }
    newUser.channelList = consolidateChannels(channelList);

    // SAVE NEW USER
    fs.writeFileSync(editsFileName, JSON.stringify(newUser) + '\n', { flag: 'a+' });
    const n_data = collection.where('userName', '==', newUser.userName).limit(1);
    const n_querySnapshot = await n_data.get();
    const n_documentSnapshot = n_querySnapshot.docs[0];
    await n_documentSnapshot.ref.update(newUser);
    console.log(`✅ Updated ${newUser.userName}`);

    // DELETE ALL RECORDS IN OLD USER LIST
    for (const key in oldUserList) {
        const oldUser = oldUserList[key];
        fs.writeFileSync(deletesFileName, JSON.stringify(oldUser) + '\n', { flag: 'a+' });

        const o_data = collection.where('userName', '==', oldUser.userName).limit(1);
        const o_querySnapshot = await o_data.get();
        const o_documentSnapshot = o_querySnapshot.docs[0];
        await o_documentSnapshot.ref.delete();
        console.log(`❌ Deleted ${oldUser.userName}`);
    }
}

function consolidateChannels(channelList) {
    const channelIdList = [];
    for (const index in channelList) {
        channelIdList.push(channelList[index].channelId);
    }
    const uniqueChannelIdList = [...new Set(channelIdList)];
    uniqueChannelIdList.sort();

    const rebuiltChannelList = [];
    for (const index in uniqueChannelIdList) {
        rebuiltChannelList.push({ channelId: uniqueChannelIdList[index] });
    }
    return rebuiltChannelList;
}

const returnOnlyDuplicates = (list) => list.filter((value, index) => list.indexOf(value) !== index);
