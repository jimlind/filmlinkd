'use strict';

class DiscordConnection {
    connected = false;
    locked = false;

    /**
     * @param {import('../../models/config')} config
     * @param {import('discord.js').Client} discordClient
     * @param {typeof import('@discordjs/rest').REST} discordRest
     * @param {typeof import('discord-api-types/v9').Routes} discordRoutes
     * @param {import('../logger')} logger
     */
    constructor(config, discordClient, discordRest, discordRoutes, logger) {
        this.config = config;
        this.discordClient = discordClient;
        this.discordRest = discordRest;
        this.discordRoutes = discordRoutes;
        this.logger = logger;
    }

    getConnectedClient() {
        return new Promise((resolve, reject) => {
            // If no token set reject the request
            if (!this.config.discordBotToken) {
                return reject('No Discord Bot Token Set');
            }

            // If the client is connected return it
            if (this.connected) {
                return resolve(this.discordClient);
            }

            // If the connecting process is happening reject additional attempts
            // Multiple connections means something terrible has happened
            if (this.locked) {
                return reject();
            }

            // Indicate the connecting process is active
            this.locked = true;

            // On ready needs to be setup before login is called or ready events may be missed
            this.discordClient.on('ready', () => {
                this.connected = true;
                this.locked = false;

                return resolve(this.discordClient);
            });
            this.discordClient.login(this.config.discordBotToken);
        });
    }
}

module.exports = DiscordConnection;
