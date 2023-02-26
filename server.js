#!/usr/bin/env node

const config = require('./config.js');
const container = require('./dependency-injection-container')(config);

if (config.get('vip')) {
    const botVipClass = require('./process/bot-vip.js');
    const botVip = new botVipClass(container);
    botVip.run();

    return;
}
