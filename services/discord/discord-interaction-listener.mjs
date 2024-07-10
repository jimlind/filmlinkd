export default class DiscordInteractionListener {
    /**
     * @param {import('../discord/discord-connection')} discordConnection
     * @param {import('discord.js')} discordLibrary
     * @param {import('../../services/logger.mjs')} logger
     */
    constructor(discordConnection, discordLibrary, logger) {
        this.discordConnection = discordConnection;
        this.discordLibrary = discordLibrary;
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
                    if (!(interaction.member instanceof this.discordLibrary.GuildMember)) return;

                    return interaction
                        .deferReply()
                        .then(() => {
                            callback(interaction);
                        })
                        .catch((error) => {
                            const data = { error, interaction };
                            this.logger.warn('Unable to defer reply on interaction.', data);
                        });
                },
            );
        });
    }
}
