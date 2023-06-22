const { ChannelType } = require('discord.js');

class DiscordMessageSender {
    constructor(discordConnection, logger) {
        this.discordConnection = discordConnection;
        this.logger = logger;
    }

    send(channelId, embed) {
        return new Promise((resolve, reject) => {
            this.discordConnection
                .getConnectedClient()
                .then((client) => {
                    return client.channels.fetch(channelId);
                })
                .then((channel) => {
                    if (!channel) {
                        // Reject without logging because not being able to fetch a channel is
                        // totally normal in a sharded environment.
                        return reject();
                    }

                    // Text and News channels can get posts
                    const textChannelTypes = [ChannelType.GuildAnnouncement, ChannelType.GuildText];
                    if (!textChannelTypes.includes(channel.type)) {
                        const metadata = {
                            channelId,
                            messageEmbed: embed.toJSON(),
                        };
                        this.logger.warn('Unable to Send Message: Not Text Channel', metadata);
                        return reject();
                    }

                    if (!channel.viewable) {
                        const metadata = {
                            channelId,
                            messageEmbed: embed.toJSON(),
                        };
                        this.logger.warn('Unable to Send Message: Channel Not Visible', metadata);
                        return reject();
                    }

                    channel
                        .send({ embeds: [embed] })
                        .then(() => {
                            const metadata = {
                                channelId,
                                guild: channel.guild.name,
                                channel: channel.name,
                                messageEmbed: embed.toJSON(),
                            };
                            this.logger.debug('Successfully Sent Message', metadata);
                            return resolve();
                        })
                        .catch(() => {
                            const metadata = {
                                channelId,
                                guild: channel.guild.name,
                                channel: channel.name,
                                messageEmbed: embed.toJSON(),
                            };
                            this.logger.warn(
                                'Unable to Send Message: Bad Channel Permissions',
                                metadata,
                            );
                            return reject();
                        });
                })
                .catch((e) => {
                    return reject();
                });
        });
    }
}

module.exports = DiscordMessageSender;
