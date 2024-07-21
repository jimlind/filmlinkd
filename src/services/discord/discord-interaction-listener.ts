import * as discord from 'discord.js';
import Logger from '../logger.js';
import DiscordConnection from './discord-connection.js';

export default class DiscordInteractionListener {
    constructor(
        readonly discordConnection: DiscordConnection,
        readonly discordLibrary: typeof discord,
        readonly logger: Logger,
    ) {}

    onInteraction(callback: any) {
        // Get connected client and listen for interactions
        return this.discordConnection.getConnectedClient().then((client: discord.Client<true>) => {
            client.on('interactionCreate', (interaction: discord.Interaction) => {
                // Ignore if not a command and not from a guild member
                if (!interaction.isCommand()) return;
                if (!(interaction.member instanceof this.discordLibrary.GuildMember)) return;

                return interaction
                    .deferReply()
                    .then(() => {
                        callback(interaction);
                    })
                    .catch((error: unknown) => {
                        const data = { error, interaction };
                        this.logger.warn('Unable to defer reply on interaction.', data);
                    });
            });
        });
    }
}
