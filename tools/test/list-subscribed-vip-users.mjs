#!/usr/bin/env node

import { URL } from 'url';
import config from '../../config.mjs';
import container from '../../dependency-injection-container.mjs';

// Configure as production
const dir = new URL('.', import.meta.url).pathname;
config.loadFile(dir + '../../config/production.json');
config.set('googleCloudIdentityKeyFile', dir + '../../.gcp-key.json');

// Initialize container
const initializedContainer = await container(config).initialize();

// ...and go!
run(initializedContainer);

async function run(initializedContainer) {
    const firestoreVipDao = initializedContainer.resolve('firestoreVipDao');
    const firestoreCollection = initializedContainer.resolve('firestoreConnection').getCollection();

    const channelData = await firestoreVipDao.read();
    const channelPromiseList = channelData.map((data) => {
        const input = { channelId: data?.channelId };
        const query = firestoreCollection
            .where('channelList', 'array-contains', input)
            .limit(data.limit);
        return query.get();
    });

    await Promise.all(channelPromiseList).then((querySnapshotList) => {
        querySnapshotList.forEach((querySnapshot) => {
            const size = querySnapshot.size;
            const limit = querySnapshot.query._queryOptions.limit;
            const channelId = querySnapshot.query._queryOptions.filters[0].value.channelId;

            console.log(`Found ${size} of ${limit} allowed accounts for ${channelId}`);
            console.log('-'.repeat(50));

            const userNameList = querySnapshot.docs.reduce((accumulator, current) => {
                return accumulator.concat([current.data().userName]);
            }, []);

            console.log(userNameList.join(', '));
            console.log('\n');
        });
    });
}
