#!/usr/bin/env node

// Allow switching between dev and prod
process.env.npm_config_live = process.argv[2] == 'prod' || false;
const config = require('../../config.js');
if (process.env.npm_config_live) {
    config.set('googleCloudIdentityKeyFile', './.gcp-key.json');
}

// ...and go!
const container = require('../../dependency-injection-container.js')(config);
const dao = container.resolve('firestoreSubscriptionDao');
run(dao);

async function run(dao) {
    const userList = await dao.getVipSubscriptions();
    const userData = userList.reduce(
        (data, user) => Object.assign(data, { [user.letterboxdId]: user?.previous?.lid || '' }),
        {},
    );
    console.log(userData);
}
