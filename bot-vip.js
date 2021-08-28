#!/usr/bin/env node

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

// Make sure the promises for PubSub and Discord Clients connect
const container = new DependencyInjectionContainer(configModel);

// This is the part that posts RSS updates at a regular interval
// Keeps track of if an active diary entry thread is running
let threadRunning = false;
let interval = null;
let index = 0;

Promise.all([container.resolve('pubSubConnection').getSubscription()]).then(
    ([pubSubSubscription]) => {
        interval = setInterval(() => {
            if (threadRunning) return; // Exit early
            container
                .resolve('diaryEntryProcessor')
                .processPageOfVipEntries(index, 25)
                .then((pageCount) => {
                    threadRunning = false;
                    index = pageCount === 0 ? 0 : index + pageCount;
                });

            threadRunning = true;

            // Attempt to stop memory leak
            return;
        }, 100); // Allow bot to rest for 100ms

        // Clean up when process is told to end
        death((signal, error) => {
            clearInterval(interval);
            pubSubSubscription.close();
            container.resolve('logger').info('Program Terminated', { signal, error });
        });
    },
);
