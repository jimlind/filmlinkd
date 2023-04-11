'use strict';

const { LoggingWinston } = require('@google-cloud/logging-winston');
const { PubSub } = require('@google-cloud/pubsub');
const { SecretManagerServiceClient } = require('@google-cloud/secret-manager').v1;
const awilix = require('awilix');
const axios = require('axios').default;
const { Client: DiscordClient, GatewayIntentBits } = require('discord.js');
const domSerializer = require('dom-serializer').default;
const htmlparser2 = require('htmlparser2');
const truncateMarkdown = require('markdown-truncate');
const turndown = require('turndown');
const winston = require('winston');

class DependencyInjectionContainer {
    /**
     * @param {import('convict').Config} config
     */
    constructor(config) {
        this.container = awilix.createContainer();

        // Create Discord Client
        const discordClient = new DiscordClient({
            intents: [GatewayIntentBits.Guilds],
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
                app: config.get('packageName'),
                version: config.get('packageVersion') + (config.get('live') ? '' : '-dev'),
            },
            prefix: config.get('live') ? null : 'DEV',
            projectId: config.get('googleCloudProjectId'),
            keyFilename: config.get('googleCloudIdentityKeyFile'),
        });

        // Create logger for the JS console
        const consoleTransport = new winston.transports.Console();

        // Create configured Turndown service
        const turndownService = new turndown();
        turndownService.addRule('break', {
            filter: ['br'],
            replacement: () => '\n',
        });
        turndownService.addRule('paragraph', {
            filter: ['p'],
            replacement: (content) => content + '\n',
        });
        turndownService.addRule('blockquote', {
            filter: ['blockquote'],
            replacement: (content) =>
                '> ' + content.split(/\r?\n/).filter(Boolean).join('\n> ') + '\n',
        });

        // Create PubSub
        const pubsub = new PubSub({
            projectId: config.get('googleCloudProjectId'),
            keyFilename: config.get('googleCloudIdentityKeyFile'),
        });

        // Create configured Secret Manager client
        const secretManagerClient = new SecretManagerServiceClient({
            keyFilename: config.get('googleCloudIdentityKeyFile'),
        });

        this.container.register({
            config: awilix.asValue(config),
            discordClient: awilix.asValue(discordClient),
            axios: awilix.asValue(axios),
            domSerializer: awilix.asValue(domSerializer),
            htmlParser2: awilix.asValue(htmlparser2),
            googleCloudWinstonTransport: awilix.asValue(googleCloudWinstonTransport),
            consoleTransport: awilix.asValue(consoleTransport),
            turndownService: awilix.asValue(turndownService),
            truncateMarkdown: awilix.asValue(truncateMarkdown),
            winston: awilix.asValue(winston),
            pubSub: awilix.asValue(pubsub),
            secretManagerClient: awilix.asValue(secretManagerClient),
        });

        this.container.loadModules(['commands/**/*.js', 'factories/**/*.js', 'services/**/*.js'], {
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

module.exports = (config) => new DependencyInjectionContainer(config);
