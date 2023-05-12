#!/usr/bin/env node

const { PermissionsBitField } = require('discord.js');
const fs = require('fs');

// Use production data but use the key so it can be accessed remotely
process.env.npm_config_live = true;
const config = require('../config.js');
config.set('googleCloudIdentityKeyFile', './.gcp-key.json');

// ...and go!
const container = require('../dependency-injection-container')(config);
const collection = container.resolve('firestoreConnection').getCollection();
processData(collection);

async function processData(collection) {
    const querySnapshot = await collection.get();
    let channelList = querySnapshot.docs.reduce(reduceDocsToChannels, []).sort();
    channelList = [...new Set(channelList)];

    const client = await container.resolve('discordConnection').getConnectedAutoShardedClient();
    const fileName = 'Bad Channel Deletes ' + new Date().toString() + '.txt';

    for (var i = 0; i < 10; i++) {
        console.log(`------------------------------`);
        console.log(`${channelList.length} channels to check...`);
        console.log(`------------------------------`);
        for (const index in channelList) {
            const channelId = channelList[index];
            let botPermissions = null;
            try {
                const channel = await client.channels.fetch(channelId);
                botPermissions = channel?.permissionsFor(client.user || '');
            } catch (error) {}

            const hasPermissions = botPermissions?.has([
                PermissionsBitField.Flags.SendMessages,
                PermissionsBitField.Flags.EmbedLinks,
            ]);

            if (hasPermissions) {
                console.log(`✅ Channel ${channelId} is all good`);
                delete channelList[index];
            } else {
                console.log(`❌ Channel ${channelId} failed checks`);
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
