#!/usr/bin/env node

console.log('Disabled until after I can sort out sharding completely.');
return;

const ConfigFactory = require('./factories/config-factory');
const DependencyInjectionContainer = require('./dependency-injection-container');
const death = require('death');
const dotenv = require('dotenv');
const fs = require('fs');

// @ts-ignore TypeScript validation doesn't like json files as a module
const packageJson = require('./package.json');

// Load .env into process.env, create config, create container
dotenv.config();
const configModel = new ConfigFactory(
    process.argv[2],
    process.env,
    packageJson,
    fs.existsSync,
).build();

const container = new DependencyInjectionContainer(configModel);
const botRestingTime = 10000; // 10 seconds

// Keeps track of if an active diary entry thread is running
let threadRunning = false;
// Keeps track of the process that has a recurring interval
let interval = null;
// Keeps track of the pagination for diary processor
let index = 0;

Promise.all([container.resolve('pubSubConnection').getSubscription()]).then(
    ([pubSubSubscription]) => {
        const startTime = Date.now();
        interval = setInterval(() => {
            if (Date.now() > startTime + 12 * 60 * 60000) {
                container.resolve('logger').info('12 Hour Reset');
                return process.exit();
            }

            // This variable is reset after a page of entries processing has completed
            if (threadRunning) return;
            threadRunning = true;

            container
                .resolve('diaryEntryProcessor')
                .processPageOfVipEntries(index, 30)
                .then((pageCount) => {
                    threadRunning = false;
                    index = pageCount === 0 ? 0 : index + pageCount;
                });
        }, botRestingTime);

        // Clean up when process is told to end
        death((signal, error) => {
            clearInterval(interval);
            pubSubSubscription.close();
            container.resolve('logger').info('Program Terminated', { signal, error });
        });
    },
);
