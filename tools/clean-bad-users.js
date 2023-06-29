#!/usr/bin/env node

// Allow switching between dev and prod
process.env.npm_config_live = process.argv[2] == 'prod' || false;
const config = require('../config.js');
if (process.env.npm_config_live) {
    config.set('googleCloudIdentityKeyFile', './.gcp-key.json');
}

// ...and go!
const container = require('../dependency-injection-container.js')(config);
const httpClient = container.resolve('httpClient');
const collection = container.resolve('firestoreConnection').getCollection();
processData(collection);

async function processData(collection) {
    // hard code some limits here so I can do the process in steps
    const querySnapshot = await collection.orderBy('userName').startAt('a').endAt('b').get();
    var documentList = querySnapshot.docs;

    for (var i = 0; i < 10; i++) {
        console.log(`${documentList.length} users to check...`);
        for (const key in documentList) {
            const data = documentList[key].data();

            const validLetterBoxdId = await getUserLetterboxdStatus(data.letterboxdId);
            if (validLetterBoxdId) {
                documentList[key] = false;
                console.log(`👉 ${data.userName}/${data.letterboxdId} validated via user lid`);
            } else {
                console.log(`❌ ${data.userName}/${data.letterboxdId} not validated via user lid`);
            }
        }
        documentList = documentList.filter((x) => !!x);
    }

    console.log(`\n👉 Updating Records`);
    for (const key in documentList) {
        const document = documentList[key];
        const id = documentList[key].id;
        const data = document.data();

        let userLid = await getUserNameStatus(data.userName);
        if (userLid == '') {
            console.log(`❌ ${data.userName} not able to validate user name`);
            userLid = await getUserNameStatus(id);
        }

        if (userLid) {
            const realUserName = await getUserLetterboxdStatus(userLid);
            console.log(`✅ Updating record for /${id}/ with ${realUserName}/${userLid}`);

            data.letterboxdId = userLid;
            data.userName = realUserName;

            // const documentSnapshot = querySnapshot.docs[key];
            await document.ref.update(data);
        } else {
            console.log(`❌ /${id}/ not able to validate with document key`);
            await document.ref.delete();
        }
    }
}

async function getUserLetterboxdStatus(letterboxdId) {
    return httpClient
        .head('https://boxd.it/' + letterboxdId, 10000)
        .then((response) => {
            if (response?.headers['x-letterboxd-type'] != 'Member') {
                throw 'error';
            }

            const url = response?.request?.path;
            return url.replaceAll('/', '');
        })
        .catch(() => {
            return '';
        });
}

async function getUserNameStatus(userName) {
    return httpClient
        .head('https://letterboxd.com/' + userName, 10000)
        .then((response) => {
            if (response?.headers['x-letterboxd-type'] != 'Member') {
                throw 'error';
            }
            return response?.headers['x-letterboxd-identifier'] || '';
        })
        .catch(() => {
            return '';
        });
}
