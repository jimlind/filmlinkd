#!/usr/bin/env node

// Allow switching between dev and prod
process.env.npm_config_live = process.argv[2] == 'prod' || false;
const config = require('../../config.js');
if (process.env.npm_config_live) {
    config.set('googleCloudIdentityKeyFile', './.gcp-key.json');
}

// ...and go!
const container = require('../../dependency-injection-container.js')(config);
const userListClass = container.resolve('subscribedUserList');
run(userListClass);

async function run(userListClass) {
    const subscriberListObject = await userListClass.getVipActiveSubscriptions();
    console.log(subscriberListObject);
}
