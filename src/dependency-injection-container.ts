import { Firestore } from '@google-cloud/firestore';
import { LoggingWinston } from '@google-cloud/logging-winston';
import { PubSub } from '@google-cloud/pubsub';
import { SecretManagerServiceClient } from '@google-cloud/secret-manager';
import { asValue, createContainer, InjectionMode, Lifetime } from 'awilix';
import axios from 'axios';
import crypto from 'crypto';
import * as discord from 'discord.js';
import exitHook from 'exit-hook';
import { LRUCache } from 'lru-cache';
import truncateMarkdown from 'markdown-truncate';
import pLimit from 'p-limit';
import { clearIntervalAsync, setIntervalAsync } from 'set-interval-async/fixed';
import turndown from 'turndown';
import { URL } from 'url';
import * as uuid from 'uuid';
import winston from 'winston';

class DependencyInjectionContainer {
    container: any;

    /**
     * @param {import('convict').Config} config
     */
    constructor(readonly config: any) {}

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
            prefix: this.config.get('live') ? undefined : 'DEV',
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
            replacement: (content: string) => content + '\n',
        });
        turndownService.addRule('blockquote', {
            filter: ['blockquote'],
            replacement: (content: string) =>
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

        // Setup 10,000 item cache object
        const cache = new LRUCache({ max: 10000 });

        this.container.register({
            cache: asValue(cache),
            config: asValue(this.config),
            crypto: asValue(crypto),
            discordClient: asValue(discordClient),
            discordLibrary: asValue(discord),
            exitHook: asValue(exitHook),
            firestoreLibrary: asValue(Firestore),
            axios: asValue(axios),
            googleCloudWinstonTransport: asValue(googleCloudWinstonTransport),
            consoleTransport: asValue(consoleTransport),
            pLimit: asValue(pLimit),
            turndownService: asValue(turndownService),
            truncateMarkdown: asValue(truncateMarkdown),
            uuid: asValue(uuid),
            winston: asValue(winston),
            pubSub: asValue(pubsub),
            secretManagerClient: asValue(secretManagerClient),
            setIntervalAsync: asValue(setIntervalAsync),
            clearIntervalAsync: asValue(clearIntervalAsync),
        });

        const dir = new URL('.', import.meta.url).pathname;
        return this.container.loadModules([dir + '(commands|factories|services)/**/*.(m)?js'], {
            esModules: true,
            formatName: 'camelCase',
            resolverOptions: {
                lifetime: Lifetime.SINGLETON,
                injectionMode: InjectionMode.CLASSIC,
            },
        });
    }

    resolve(connectionString: any) {
        return this.container.resolve(connectionString);
    }
}

export default (config: any) => new DependencyInjectionContainer(config);
