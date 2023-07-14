import { Firestore } from '@google-cloud/firestore';
import { LoggingWinston } from '@google-cloud/logging-winston';
import { PubSub } from '@google-cloud/pubsub';
import { SecretManagerServiceClient } from '@google-cloud/secret-manager';
import { asValue, createContainer, InjectionMode, Lifetime } from 'awilix';
import axios from 'axios';
import crypto from 'crypto';
import death from 'death';
import * as discord from 'discord.js';
import * as htmlparser2 from 'htmlparser2';
import truncateMarkdown from 'markdown-truncate';
import pLimit from 'p-limit';
import turndown from 'turndown';
import * as uuid from 'uuid';
import winston from 'winston';

class DependencyInjectionContainer {
    /**
     * @param {import('convict').Config} config
     */
    constructor(config) {
        this.config = config;
    }

    /**
     * @returns {Promise<import('awilix').AwilixContainer>}
     */
    initialize() {
        this.container = createContainer();

        // Create Discord Client
        const discordClient = new discord.Client({
            intents: [discord.GatewayIntentBits.Guilds],
            presence: {
                status: 'online',
                activities: [
                    {
                        name: 'Slash Commands',
                        type: discord.ActivityType.Listening,
                        url: 'https://jimlind.github.io/filmlinkd/',
                    },
                ],
            },
        });

        // Create logger transport for the GCP console
        const googleCloudWinstonTransport = new LoggingWinston({
            labels: {
                app: this.config.get('packageName'),
                version:
                    this.config.get('packageVersion') + (this.config.get('live') ? '' : '-dev'),
            },
            prefix: this.config.get('live') ? null : 'DEV',
            projectId: this.config.get('googleCloudProjectId'),
            keyFilename: this.config.get('googleCloudIdentityKeyFile'),
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
            projectId: this.config.get('googleCloudProjectId'),
            keyFilename: this.config.get('googleCloudIdentityKeyFile'),
        });

        // Create configured Secret Manager client
        const secretManagerClient = new SecretManagerServiceClient({
            keyFilename: this.config.get('googleCloudIdentityKeyFile'),
        });

        this.container.register({
            config: asValue(this.config),
            crypto: asValue(crypto),
            death: asValue(death),
            discordClient: asValue(discordClient),
            discordLibrary: asValue(discord),
            firestoreLibrary: asValue(Firestore),
            axios: asValue(axios),
            htmlParser2: asValue(htmlparser2),
            googleCloudWinstonTransport: asValue(googleCloudWinstonTransport),
            consoleTransport: asValue(consoleTransport),
            pLimit: asValue(pLimit),
            turndownService: asValue(turndownService),
            truncateMarkdown: asValue(truncateMarkdown),
            uuid: asValue(uuid),
            winston: asValue(winston),
            pubSub: asValue(pubsub),
            secretManagerClient: asValue(secretManagerClient),
        });

        return this.container.loadModules(['(commands|factories|services)/**/*.(m)?js'], {
            esModules: true,
            formatName: 'camelCase',
            resolverOptions: {
                lifetime: Lifetime.SINGLETON,
                injectionMode: InjectionMode.CLASSIC,
            },
        });
    }

    resolve(connectionString) {
        return this.container.resolve(connectionString);
    }
}

export default (config) => new DependencyInjectionContainer(config);
