export default class DiscordMessageSender {
    constructor(
        readonly discordConnection: any,
        readonly discordLibrary: any,
        readonly logger: any,
    ) {}

    /**
     * @param {string} channelId
     * @param {import { Embed } from "discord.js";} embed
     * @returns Promise<any>
     */
    send(channelId: any, embed: any) {
        // Message metadata for logging any issues.
        const metadata = { channelId, messageEmbed: embed.toJSON() };

        // Store this promise as a variable for reuse
        const fetchChannelPromise = this.discordConnection
            .getConnectedClient()
            .then((client: any) => client.channels.fetch(channelId))
            .catch(() => null);

        return fetchChannelPromise
            .then((channel: any) => {
                // Reject without logging because not being able to fetch a channel is
                // totally normal in a sharded environment.
                if (!channel) return Promise.all([]);

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
                    return Promise.all([]);
                }
                if (!channel.viewable) {
                    const message = 'Unable to Send Message: Channel Not Visible';
                    this.logger.warn(message, metadata);
                    return Promise.all([]);
                }

                return Promise.all([fetchChannelPromise, channel.send({ embeds: [embed] })]);
            })
            .then(([channel, sendResult]: any) => {
                // Things didn't go as planned throw something specifially to be caught.
                if (!channel || !sendResult) {
                    throw 'Unable to Send Message';
                }

                // Message metadata for logging success.
                const successMetadata = {
                    channelId,
                    guild: channel.guild.name,
                    channel: channel.name,
                    messageEmbed: embed.toJSON(),
                };
                this.logger.debug('Successfully Sent Message', successMetadata);
            })
            .catch(() => {
                this.logger.warn('Unable to Send Message', metadata);
            });
    }
}
