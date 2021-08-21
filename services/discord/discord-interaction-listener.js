'use strict';

class DiscordInteractionListener {
    /**
     * @param {import('../discord/discord-connection')} discordConnection
     */
    constructor(discordConnection) {
        this.discordConnection = discordConnection;
    }

    onInteraction(callback) {
        // Get connected client and listen for interactions
        return this.discordConnection.getConnectedClient().then((client) => {
            client.on(
                'interactionCreate',
                (/** @type {import("discord.js").Interaction} */ interaction) => {
                    // Ignore if not a command and not from a guild member
                    if (!interaction.isCommand()) return;
                    if (!(interaction.member instanceof require('discord.js').GuildMember)) return;

                    return callback(interaction);
                },
            );
        });
    }
}

module.exports = DiscordInteractionListener;
