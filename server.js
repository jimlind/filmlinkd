#!/usr/bin/env node

const config = require('./config.js');
const container = require('./dependency-injection-container')(config);

require('death')((signal, error) => {
    container.resolve('logger').info('Program Terminated', { signal, error });
    // TODO: Close any of the pubsub connections if they are open
});

if (config.get('mode') === 'solo') {
    const standaloneClass = require('./process/standalone.js');
    const standalone = new standaloneClass(container);
    standalone.run();

    const scraperClass = require('./process/scraper.js');
    const scraper = new scraperClass(container);
    scraper.run();

    return;
}

if (config.get('mode') === 'vip') {
    const vipClass = require('./process/vip.js');
    const vip = new vipClass(container);
    vip.run();

    return;
}
