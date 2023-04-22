'use strict';

class DiscordConnection {
    connected = false;
    locked = false;

    /**
     * @param {import('../../models/config')} config
     * @param {import('discord.js').Client} discordClient
     * @param {import('../logger')} logger
     * @param {import('../google/secret-manager')} secretManager
     */
    constructor(config, discordClient, logger, secretManager) {
        this.config = config;
        this.discordClient = discordClient;
        this.logger = logger;
        this.secretManager = secretManager;
    }

    getConnectedClient() {
        // TODO: The secret manager is only used the first time.
        // I should fix that eventually.
        return this.secretManager.getValue(this.config.get('discordBotTokenName')).then((token) => {
            // If no token set reject the request
            if (!token) {
                throw new Error('No Discord Bot Token Set');
            }

            // If the client is connected return it
            if (this.connected) {
                return this.discordClient;
            }

            // If this method is locked wait until the topic is set
            if (this.locked) {
                const interval = setInterval(() => {
                    if (this.connected) {
                        clearInterval(interval);
                        return this.discordClient;
                    }
                }, 200); // Completely arbitrary timing choice
            }

            // Indicate the connecting process is active
            this.locked = true;

            // On ready needs to be setup before login is called or ready events may be missed
            return new Promise((resolve) => {
                this.discordClient.on('ready', () => {
                    this.connected = true;
                    this.locked = false;

                    this.logger.info('Discord Client is Ready');
                    resolve(this.discordClient);
                });
                this.discordClient.login(token);
            });
        });
    }
}

module.exports = DiscordConnection;
