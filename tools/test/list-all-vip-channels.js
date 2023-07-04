#!/usr/bin/env node

process.env.npm_config_live = process.argv[2] == 'prod' || false;
const config = require('../../config.js');
if (process.env.npm_config_live) {
    config.set('googleCloudIdentityKeyFile', './.gcp-key.json');
}

// ...and go!
const container = require('../../dependency-injection-container.js')(config);
const dao = container.resolve('firestoreVipDao');
run(dao);

async function run(dao) {
    const data = await dao.read();
    console.log(data);
}
