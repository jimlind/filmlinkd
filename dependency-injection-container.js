'use strict';

const { REST: DiscordRest } = require('@discordjs/rest');
const { LoggingWinston } = require('@google-cloud/logging-winston');
const { PubSub } = require('@google-cloud/pubsub');
const awilix = require('awilix');
const axios = require('axios').default;
const { Routes: DiscordRoutes } = require('discord-api-types/v9');
const { Client: DiscordClient, Intents: DiscordIntents } = require('discord.js');
const htmlparser2 = require('htmlparser2');
const truncateMarkdown = require('markdown-truncate');
const turndown = require('turndown');
const winston = require('winston');

class DependencyInjectionContainer {
    constructor(configModel) {
        this.container = awilix.createContainer();

        // Create Discord Client
        const discordClient = new DiscordClient({
            intents: [DiscordIntents.FLAGS.GUILDS],
            presence: {
                status: 'online',
                activities: [
                    {
                        name: 'Slash Commands',
                        type: 'LISTENING',
                        url: 'https://jimlind.github.io/filmlinkd/',
                    },
                ],
            },
        });

        // Create logger transport for the GCP console
        const googleCloudWinstonTransport = new LoggingWinston({
            labels: {
                app: configModel.packageName,
                version: configModel.packageVersion + (configModel.isDev ? '-dev' : ''),
            },
            prefix: configModel.isDev ? 'DEV' : null,
            projectId: configModel.googleCloudProjectId,
            keyFilename: configModel.gcpKeyFile,
        });

        // Create logger for the JS console
        const consoleTransport = new winston.transports.Console();

        // Creast PubSub
        const pubsub = new PubSub({
            projectId: configModel.googleCloudProjectId,
            keyFilename: configModel.gcpKeyFile,
        });

        this.container.register({
            config: awilix.asValue(configModel),
            discordClient: awilix.asValue(discordClient),
            discordRest: awilix.asValue(DiscordRest),
            discordRoutes: awilix.asValue(DiscordRoutes),
            axios: awilix.asValue(axios),
            htmlParser2: awilix.asValue(htmlparser2),
            googleCloudWinstonTransport: awilix.asValue(googleCloudWinstonTransport),
            consoleTransport: awilix.asValue(consoleTransport),
            turndownService: awilix.asValue(new turndown()),
            truncateMarkdown: awilix.asValue(truncateMarkdown),
            winston: awilix.asValue(winston),
            pubSub: awilix.asValue(pubsub),
        });

        this.container.loadModules(['factories/**/*.js', 'services/**/*.js', 'compiled/**/*.js'], {
            formatName: 'camelCase',
            resolverOptions: {
                lifetime: awilix.Lifetime.SINGLETON,
                injectionMode: awilix.InjectionMode.CLASSIC,
            },
        });
    }

    resolve(connectionString) {
        return this.container.resolve(connectionString);
    }
}

module.exports = DependencyInjectionContainer;
