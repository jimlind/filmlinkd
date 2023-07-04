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
    const subscriberListObject22 = await userListClass.getActiveVipSubscriptionsPage(2, 2);
    console.log(subscriberListObject22);
    const subscriberListObject32 = await userListClass.getActiveVipSubscriptionsPage(3, 2);
    console.log(subscriberListObject32);
}
