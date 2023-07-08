#!/usr/bin/env node

import config from './config.js';
import container from './dependency-injection-container.js';
import { Scraper } from './process/scraper.mjs';
import Cluster from './process/sharding/cluster.js';
import StandAlone from './process/standalone.js';
import { Vip } from './process/vip.mjs';

switch (config.get('mode')) {
    case 'sharded':
        const cluster = new Cluster(config);
        cluster.run();
        break;

    case 'scraper':
        const solorScraper = new Scraper(container(config));
        solorScraper.run();
        break;

    case 'vip':
        const vip = new Vip(container(config));
        vip.run();
        break;

    default:
        const standalone = new StandAlone(container(config));
        standalone.run();
        const scraper = new Scraper(container(config));
        scraper.run();
        break;
}
