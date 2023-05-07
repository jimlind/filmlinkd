#!/usr/bin/env node
const config = require('./config.js');

// Run the clustering class without dependency injection because that large object
// isn't needed for cluster management
if (config.get('mode') === 'sharded') {
    const clusterClass = require('./process/sharding/cluster.js');
    const cluster = new clusterClass(config);
    cluster.run();

    return;
}

// Create the dependency injection container now for all single thread operations.
const container = require('./dependency-injection-container')(config);

require('death')((signal, error) => {
    container.resolve('logger').info('Program Terminated', { signal, error });
    // Ensure all the PubSub connections are closed
    container.resolve('pubSubConnection').closeLogEntrySubscription();
    container.resolve('pubSubConnection').closeLogEntryResultSubscription();
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

if (config.get('mode') === 'scraper') {
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
