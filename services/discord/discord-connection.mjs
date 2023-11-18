export default class DiscordConnection {
    /** @type {boolean} */
    connected = false;
    /** @type {boolean} */
    locked = false;

    /**
     * @param {import('../../models/config')} config
     * @param {import('discord.js').Client} discordClient
     * @param {import('../logger.mjs')} logger
     * @param {import('../google/secret-manager.mjs')} secretManager
     */
    constructor(config, discordClient, logger, secretManager) {
        this.config = config;
        this.discordClient = discordClient;
        this.logger = logger;
        this.secretManager = secretManager;
    }

    getConnectedClient() {
        // If connected return the connected discord client
        if (this.connected) {
            return new Promise((resolve) => {
                resolve(this.discordClient);
            });
        }

        const tokenName = this.config.get('discordBotTokenName');
        return this.secretManager.getValue(tokenName).then((token) => {
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
     *
     * @returns Promise<any>
     */
    getConnectedAutoShardedClient() {
        return this.secretManager.getValue(this.config.get('discordBotTokenName')).then((token) => {
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
