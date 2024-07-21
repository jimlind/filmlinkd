import * as discord from 'discord.js';
import Logger from '../logger.js';
import DiscordConnection from './discord-connection.js';

export default class DiscordMessageSender {
    constructor(
        readonly discordConnection: DiscordConnection,
        readonly discordLibrary: typeof discord,
        readonly logger: Logger,
    ) {}

    /**
     * @param {string} channelId
     * @param {import { Embed } from "discord.js";} embed
     * @returns Promise<any>
     */
    async send(channelId: string, embed: discord.EmbedBuilder): Promise<void> {
        // Message metadata for logging any issues.
        const metadata = { channelId, messageEmbed: embed.toJSON() };

        try {
            const discorClient = await this.discordConnection.getConnectedClient();
            const channel = await discorClient.channels.fetch(channelId);

            // Reject without logging because not being able to fetch a channel is
            // totally normal in a sharded environment.
            if (!channel) return;

            // Text, Threads, and News channels are allowed channel types
            const allowedTextChannelTypes = [
                this.discordLibrary.ChannelType.GuildAnnouncement,
                this.discordLibrary.ChannelType.GuildText,
                this.discordLibrary.ChannelType.AnnouncementThread,
                this.discordLibrary.ChannelType.PrivateThread,
                this.discordLibrary.ChannelType.PublicThread,
            ];
            if (!allowedTextChannelTypes.includes(channel.type)) {
                const message = 'Unable to Send Message: Not Text Channel';
                this.logger.warn(message, metadata);
                return;
            }
            if (!channel.viewable) {
                const message = 'Unable to Send Message: Channel Not Visible';
                this.logger.warn(message, metadata);
                return;
            }

            // Full Send!
            const sendResult = await channel.send({ embeds: [embed] });
            if (!sendResult) {
                throw 'Unable to Send Message';
            }

            // Log a successful message sending
            const successMetadata = {
                channelId,
                guild: channel.guild.name,
                channel: channel.name,
                messageEmbed: embed.toJSON(),
            };
            this.logger.debug('Successfully Sent Message', successMetadata);
        } catch (error) {
            this.logger.warn('Unable to Send Message', metadata);
        }

        // This isn't techncially neccessary but still feels good.
        return;
    }
}
