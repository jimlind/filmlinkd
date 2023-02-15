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
        return this.secretManager.getValue(this.config.discordBotTokenKey).then((token) => {
            // If no token set reject the request
            if (!token) {
                throw new Error('No Discord Bot Token Set');
            }

            // If the client is connected return it
            if (this.connected) {
                return this.discordClient;
            }

            // If the connecting process is happening reject additional attempts
            // Multiple connections means something terrible has happened
            if (this.locked) {
                throw new Error('Multiple Discord Client Connection Attempts');
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
