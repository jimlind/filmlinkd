#!/usr/bin/env node

import * as fs from 'fs';
import { URL } from 'url';
import config from '../../config.mjs';
import container from '../../dependency-injection-container.mjs';

// Configure as production
const dir = new URL('.', import.meta.url).pathname;
config.loadFile(dir + '../../config/production.json');
config.set('googleCloudIdentityKeyFile', dir + '../../.gcp-key.json');

// Initialize container
const initializedContainer = await container(config).initialize();

const lidWebService = initializedContainer.resolve('letterboxdLidWeb');
const deletesFileName = 'Duplicate LID Deletes ' + new Date().toString() + '.txt';
const editsFileName = 'Duplicate LID Edits ' + new Date().toString() + '.txt';

// ...and go!
const collection = initializedContainer.resolve('firestoreConnection').getCollection();
run(collection);

async function run(collection) {
    const list = [];

    const querySnapshot = await collection.get();
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

    for (const key in uniqueDuplicates) {
        const query = collection.where('letterboxdId', '==', uniqueDuplicates[key]);
        const documentSnapshotList = await query.get();

        const sameUsers = [];
        for (const key in documentSnapshotList.docs) {
            const data = documentSnapshotList.docs[key].data();
            sameUsers.push(data);
        }

        await consolidateUsers(sameUsers);
    }
}

const returnOnlyDuplicates = (list) => {
    return list.filter((value, index) => list.indexOf(value) !== index);
};

async function consolidateUsers(users) {
    const newUserList = [];
    const oldUserList = [];
    let channelList = [];

    for (const key in users) {
        const user = users[key];
        const lid = await lidWebService.get(user.userName).catch(() => '');
        if (lid) {
            newUserList.push(user);
        } else {
            oldUserList.push(user);
        }

        if (user.footer) {
            console.log(`🟡 Failure on ${users[0].letterboxdId}. Has footer data.`);
            return;
        }

        channelList = channelList.concat(user.channelList);
    }

    if (newUserList.length !== 1) {
        console.log({ newUserList });
        console.log(`🟡 Failure on ${users[0].letterboxdId}. No clean new user.`);
        return;
    }

    const newUser = newUserList[0];
    newUser.channelList = consolidateChannels(channelList);

    // SAVE NEW USER
    fs.writeFileSync(editsFileName, JSON.stringify(newUser) + '\n', { flag: 'a+' });
    const n_data = collection.where('userName', '==', newUser.userName).limit(1);
    const n_querySnapshot = await n_data.get();
    const n_documentSnapshot = n_querySnapshot.docs[0];
    await n_documentSnapshot.ref.update(newUser);
    console.log(
        `✅ Updated ${newUser.userName} lid:${newUser.letterboxdId} channels:${newUser.channelList.length}`,
    );

    for (const key in oldUserList) {
        // DELETE OLD USER
        const oldUser = oldUserList[key];
        fs.writeFileSync(deletesFileName, JSON.stringify(oldUser) + '\n', { flag: 'a+' });

        const o_data = collection.where('userName', '==', oldUser.userName).limit(1);
        const o_querySnapshot = await o_data.get();
        const o_documentSnapshot = o_querySnapshot.docs[0];
        await o_documentSnapshot.ref.delete();
        console.log(`❌ Deleted ${oldUser.userName} channels:${oldUser.channelList.length}`);
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
