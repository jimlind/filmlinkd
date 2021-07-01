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
    const channelList = querySnapshot.docs.reduce(reduceDocsToChannels, {});
    const client = await container.resolve('discordConnection').getConnectedClient();
    const fileName = 'Bad Channel Deletes ' + new Date().toString() + '.txt';

    for (var i = 0; i < 10; i++) {
        console.log(`${Object.values(channelList).length} channels to check...`);
        for (const channelId in channelList) {
            const channel = client.channels.cache.find((ch) => ch.id === channelId);
            const botPermissions = channel?.permissionsFor(client.user || '');
            if (botPermissions?.has(['VIEW_CHANNEL', 'SEND_MESSAGES', 'EMBED_LINKS'])) {
                console.log(`+++ Success with ${channelId}`);
                delete channelList[channelId];
            } else {
                console.log(`--- Failure with ${channelId}`);
            }
        }
    }

    for (const channelId in channelList) {
        const guildId = channelList[channelId];
        deleteChannelUsers(channelId, guildId, fileName);
    }

    client.destroy();
}

function reduceDocsToChannels(acc, current) {
    current.data().channelList.forEach((channel) => {
        acc[channel.channelId] = channel.guildId;
    });

    return acc;
}

async function deleteChannelUsers(channelId, guildId, fileName) {
    const channel = { channelId: channelId, guildId: guildId };
    const query = collection.where('channelList', 'array-contains', channel);
    const querySnapshot = await query.get();

    for (const key in querySnapshot.docs) {
        const documentSnapshot = querySnapshot.docs[key];
        const data = documentSnapshot.data();
        const content = data.userName + '||' + channelId + '||' + guildId + '\n';
        fs.writeFileSync(fileName, content, { flag: 'a+' }, (err) => {});

        data.channelList = data.channelList.filter((channel) => {
            return channel.channelId !== channelId;
        });

        await documentSnapshot.ref.update(data);
        console.log(`Removed channel ${channelId} from user ${data.userName}`);
    }
}
