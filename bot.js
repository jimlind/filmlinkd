#!/usr/bin/env node

const ConfigFactory = require("./factories/config-factory");
const DependencyInjectionContainer = require("./dependency-injection-container");
const death = require("death");
const dotenv = require("dotenv");
const fs = require("fs");
const packageJson = require("./package.json");

// Load .env into process.env, create config, create container
dotenv.config();
const configModel = new ConfigFactory(
    process.argv[2],
    process.env,
    packageJson,
    fs.existsSync
).build();
const container = new DependencyInjectionContainer(configModel);

container
    .resolve("discordConnection")
    .getConnectedClient()
    .then((discordClient) => {
        const serverCount = discordClient.guilds.cache.size;
        container
            .resolve("logger")
            .info(`Discord Client Logged In on ${serverCount} Servers`);

        // Listen for messages posted and respond
        container.resolve("discordMessageListener").onMessage((message) => {
            container.resolve("actionTranslator").translate(message);
        });

        // This is the part that posts RSS updates at a regular interval
        // Keeps track of if an active diary entry thread is running
        var threadRunning = false;

        const diaryRestInterval = 60 * 1000; // Give it 1 minute to rest
        const interval = setInterval(() => {
            if (threadRunning) return;

            const config = container.resolve("config");
            container
                .resolve("diaryEntryWriter")
                .postPageOfEntries(config.rssDelay, config.pageSize)
                .then(() => {
                    threadRunning = false;
                });

            threadRunning = true;
        }, diaryRestInterval);

        // Clean up when process is told to end
        death((signal, error) => {
            clearInterval(interval);
            discordClient.destroy();
            container
                .resolve("logger")
                .info("Program Terminated", { signal, error });
        });
    });
