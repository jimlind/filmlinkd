#!/usr/bin/env node

import config from './config.js';
import container from './dependency-injection-container.js';
import Scraper from './process/scraper.js';
import Cluster from './process/sharding/cluster.js';
import StandAlone from './process/standalone.js';
import Vip from './process/vip.js';

switch (config.get('mode')) {
    case 'sharded':
        new Cluster(config).run();
        break;

    case 'scraper':
        const scraperAwilixContainerPromise = container(config).initialize();
        scraperAwilixContainerPromise.then((awilixContainer: any) => {
            new Scraper(awilixContainer).run();
        });
        break;

    case 'vip':
        const vipAwilixContainerPromise = container(config).initialize();
        vipAwilixContainerPromise.then((awilixContainer: any) => {
            new Vip(awilixContainer).run();
        });
        break;

    default:
        const defaultAwilixContainerPromise = container(config).initialize();
        defaultAwilixContainerPromise.then((awilixContainer: any) => {
            const standalone = new StandAlone(awilixContainer);
            standalone.run();
            const scraper = new Scraper(awilixContainer);
            scraper.run();
        });
        break;
}
