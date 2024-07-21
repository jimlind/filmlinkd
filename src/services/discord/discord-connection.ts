import { Client } from 'discord.js';
import Config from '../../config.js';
import SecretManager from '../google/secret-manager.js';
import Logger from '../logger.js';

export default class DiscordConnection {
    connected: boolean = false;
    locked: boolean = false;

    constructor(
        readonly config: typeof Config,
        readonly discordClient: Client,
        readonly logger: Logger,
        readonly secretManager: SecretManager,
    ) {}

    getConnectedClient() {
        // If connected return the connected discord client
        if (this.connected) {
            return new Promise((resolve) => {
                resolve(this.discordClient);
            });
        }

        const tokenName = this.config.get('discordBotTokenName');
        return this.secretManager.getValue(tokenName).then((token: string) => {
            if (!token) {
                throw new Error('No Discord Bot Token Set');
            }

            if (this.locked) {
                // Connecting process is active so recursively wait
                return new Promise((resolve) => {
                    const delay = 200; // Completely arbitrary timing choice
                    const getClient = () => {
                        if (this.connected) {
                            resolve(this.discordClient);
                        } else {
                            setTimeout(getClient, delay);
                        }
                    };
                    setTimeout(getClient, delay);
                });
            } else {
                // Indicate the connecting process is active with a lock
                this.locked = true;
                return new Promise((resolve) => {
                    this.discordClient.on('ready', () => {
                        this.connected = true;
                        resolve(this.discordClient);
                    });
                    this.discordClient.login(token);
                });
            }
        });
    }

    /**
     * This is a strictly utilitarian class that will get a specially setup client without any
     * of the niceties of getConnectedClient (avoiding multiple connectsion, etc).
     */
    getConnectedAutoShardedClient(): Promise<Client<true>> {
        return this.secretManager
            .getValue(this.config.get('discordBotTokenName'))
            .then((token: string) => {
                return new Promise((resolve) => {
                    this.discordClient.on('ready', () => {
                        this.logger.info('Auto Sharded Discord Client is Ready');
                        resolve(this.discordClient);
                    });
                    // Enable auto sharding on the client. This is generally accepted as not a great solution
                    this.discordClient.options.shards = 'auto';
                    this.discordClient.login(token);
                });
            });
    }
}
