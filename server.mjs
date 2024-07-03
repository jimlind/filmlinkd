#!/usr/bin/env node

import config from './config.mjs';
import container from './dependency-injection-container.mjs';
import Scraper from './process/scraper.mjs';
import Manager from './process/sharding/manager.mjs';
import StandAlone from './process/standalone.mjs';
import Vip from './process/vip.mjs';

switch (config.get('mode')) {
    case 'sharded':
        new Manager(config).run();
        break;

    case 'scraper':
        const scraperAwilixContainerPromise = container(config).initialize();
        scraperAwilixContainerPromise.then((awilixContainer) => {
            new Scraper(awilixContainer).run();
        });
        break;

    case 'vip':
        const vipAwilixContainerPromise = container(config).initialize();
        vipAwilixContainerPromise.then((awilixContainer) => {
            new Vip(awilixContainer).run();
        });
        break;

    default:
        const defaultAwilixContainerPromise = container(config).initialize();
        defaultAwilixContainerPromise.then((awilixContainer) => {
            const standalone = new StandAlone(awilixContainer);
            standalone.run();
            const scraper = new Scraper(awilixContainer);
            scraper.run();
        });
        break;
}
