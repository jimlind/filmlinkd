#!/usr/bin/env node

const config = require('./config.js');
const container = require('./dependency-injection-container')(config);

if (config.get('vip')) {
    const vipClass = require('./process/vip.js');
    const vip = new vipClass(container);
    vip.run();

    return;
}
