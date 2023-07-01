#!/usr/bin/env node

// Allow switching between dev and prod
process.env.npm_config_live = process.argv[2] == 'prod' || false;
const config = require('../../config.js');
if (process.env.npm_config_live) {
    config.set('googleCloudIdentityKeyFile', './.gcp-key.json');
}

// ...and go!
const container = require('../../dependency-injection-container')(config);
const httpClient = container.resolve('httpClient');
const collection = container.resolve('firestoreConnection').getCollection();
processData(collection);

async function processData(collection) {
    const querySnapshot = await collection.orderBy('userName').startAt('a').endAt('b').get();

    for (const key in querySnapshot.docs) {
        const documentSnapshot = querySnapshot.docs[key];
        const data = documentSnapshot.data();

        // Skip if a previous lid exists
        if (data?.previous?.lid) {
            continue;
        }

        // Skip if no previous data exists
        if (!data?.previous?.published) {
            console.log(`👉 ${data.userName}/${data.letterboxdId} has nothing previous`);
            continue;
        }

        const entryLid = await getEntryLid(data?.previous?.uri);
        if (!entryLid) {
            console.log(`❌ ${data.userName}/${data.letterboxdId} has bad previous url`);
            const previousData = await getPreviousData(data.letterboxdId);
            if (previousData) {
                data.previous = previousData;
                message = `✅ Updated ${data.userName}/${data.letterboxdId} with ${previousData.uri} (${previousData.lid})\n`;
            } else {
                data.previous = {};
                message = `✅ Updated ${data.userName}/${data.letterboxdId} with EMPTY previous\n`;
            }

            await documentSnapshot.ref.update(data);
            console.log(message);
            continue;
        }

        console.log(`❌❌❌ ${data.userName}/${data.letterboxdId} has unresolvable problems.`);
    }
}

async function getEntryLid(entryUri) {
    return httpClient
        .head(entryUri, 10000)
        .then((response) => {
            const letterboxdId = response?.headers['x-letterboxd-identifier'] || '';
            if (!letterboxdId) {
                return '';
            }
            return letterboxdId;
        })
        .catch(() => {
            return '';
        });
}

async function getPreviousData(userLid) {
    const processor = container.resolve('diaryEntryProcessor');
    entries = await processor.getNewLogEntriesForUser(userLid, '', 1);

    if (!entries.length) {
        return false;
    }

    const entry = entries[0];
    const published = new Date(entry.whenUpdated).getTime();

    return {
        id: entry.viewingId,
        lid: entry.id,
        list: [entry.id],
        published: published,
        uri: entry.links[0].url,
    };
}
