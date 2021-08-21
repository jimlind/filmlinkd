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
                this.registerCommands();

                return resolve(this.discordClient);
            });
            this.discordClient.login(this.config.discordBotToken);
        });
    }

    registerCommands() {
        const rest = new this.discordRest({ version: '9' }).setToken(this.config.discordBotToken);
        const commands = [
            {
                name: 'help',
                description: 'Replies with a some helpful information and links.',
            },
            {
                name: 'follow',
                description: 'Adds the Letterboxd account to the following list this channel.',
                options: [
                    {
                        name: 'account',
                        description: 'Letterboxd account name',
                        type: 3,
                        required: true,
                    },
                ],
            },
            {
                name: 'unfollow',
                description:
                    'Removes the Letterboxd account from the following list in this channel.',
                options: [
                    {
                        name: 'account',
                        description: 'Letterboxd account name',
                        type: 3,
                        required: true,
                    },
                ],
            },
            {
                name: 'following',
                description: 'Replies with a list of all accounts followed in this channel.',
            },
            {
                name: 'refresh',
                description: 'Updates the Filmlinkd cache for the Letterboxd account.',
                options: [
                    {
                        name: 'account',
                        description: 'Letterboxd account name',
                        type: 3,
                        required: true,
                    },
                ],
            },
        ];

        // Update global commands for eventual distribution to all guilds
        rest.put(this.discordRoutes.applicationCommands(this.config.discordClientId), {
            body: commands,
        }).catch((e) => {
            this.logger.info('Unable to set global Application Commands');
        });
    }
}

module.exports = DiscordConnection;
