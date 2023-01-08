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
    let channelList = querySnapshot.docs.reduce(reduceDocsToChannels, []).sort();
    channelList = [...new Set(channelList)];

    const client = await container.resolve('discordConnection').getConnectedClient();
    const fileName = 'Bad Channel Deletes ' + new Date().toString() + '.txt';

    for (var i = 0; i < 10; i++) {
        console.log(`------------------------------`);
        console.log(`${channelList.length} channels to check...`);
        console.log(`------------------------------`);
        for (const index in channelList) {
            const channelId = channelList[index];
            const channel = client.channels.cache.find((ch) => ch.id === channelId);
            const botPermissions = channel?.permissionsFor(client.user || '');
            if (botPermissions?.has(['SEND_MESSAGES', 'EMBED_LINKS'])) {
                console.log(`✅ Channel ${channelId} is all good`);
                delete channelList[index];
            } else if (channel) {
                console.log(`❌ Channel ${channelId} doesn't have proper permissions`);
            } else {
                console.log(`❌ Channel ${channelId} doesn't exist`);
            }
        }
        channelList = channelList.sort().filter(Boolean);
    }

    for (const index in channelList) {
        const channelId = channelList[index];
        deleteChannelUsers(channelId, fileName);
    }

    client.destroy();
}

function reduceDocsToChannels(acc, current) {
    current.data().channelList.forEach((channel) => {
        acc.push(channel.channelId);
    });
    return acc;
}

async function deleteChannelUsers(channelId, fileName) {
    let channel = { channelId: channelId };
    const query = collection.where('channelList', 'array-contains', channel);
    const querySnapshot = await query.get();

    for (const key in querySnapshot.docs) {
        const documentSnapshot = querySnapshot.docs[key];
        const data = documentSnapshot.data();
        const content = data.userName + '||' + channelId + '\n';
        fs.writeFileSync(fileName, content, { flag: 'a+' });

        const originalContent = data.userName + '||' + JSON.stringify(data.channelList) + '\n';
        fs.writeFileSync(`Original ${fileName}`, originalContent, { flag: 'a+' });

        data.channelList = data.channelList.filter((channel) => {
            return channel.channelId !== channelId;
        });

        const cleanContent = data.userName + '||' + JSON.stringify(data.channelList) + '\n';
        fs.writeFileSync(`Clean ${fileName}`, cleanContent, { flag: 'a+' });

        await documentSnapshot.ref.update(data);
        console.log(`Removed channel ${channelId} from user ${data.userName}`);
    }
}
