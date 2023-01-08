'use strict';

class DiscordInteractionListener {
    /**
     * @param {import('../discord/discord-connection')} discordConnection
     * @param {import('../../services/logger')} logger
     */
    constructor(discordConnection, logger) {
        this.discordConnection = discordConnection;
        this.logger = logger;
    }

    onInteraction(callback) {
        // Get connected client and listen for interactions
        return this.discordConnection.getConnectedClient().then((client) => {
            client.on(
                'interactionCreate',
                (/** @type {import('discord.js').Interaction} */ interaction) => {
                    // Ignore if not a command and not from a guild member
                    if (!interaction.isCommand()) return;
                    if (!(interaction.member instanceof require('discord.js').GuildMember)) return;

                    return interaction
                        .deferReply()
                        .then(() => {
                            callback(interaction);
                        })
                        .catch(() => {
                            this.logger.warn('Unable to defer reply on interaction.', interaction);
                        });
                },
            );
        });
    }
}

module.exports = DiscordInteractionListener;
