#!/usr/bin/env node

import config from './config.mjs';
import container from './dependency-injection-container.js';
import Scraper from './process/scraper.mjs';
import Cluster from './process/sharding/cluster.js';
import StandAlone from './process/standalone.mjs';
import Vip from './process/vip.mjs';

switch (config.get('mode')) {
    case 'sharded':
        const cluster = new Cluster(config);
        cluster.run();
        break;

    case 'scraper':
        const scraperAwilixContainerPromise = container(config).initialize();
        scraperAwilixContainerPromise.then((container) => {
            new Scraper(container).run();
        });
        break;

    case 'vip':
        const vipAwilixContainerPromise = container(config).initialize();
        vipAwilixContainerPromise.then((container) => {
            new Vip(container).run();
        });
        break;

    default:
        const defaultAwilixContainerPromise = container(config).initialize();
        defaultAwilixContainerPromise.then((container) => {
            const standalone = new StandAlone(container);
            standalone.run();
            const scraper = new Scraper(container);
            scraper.run();
        });
        break;
}
