const { ChannelType } = require('discord.js');

class DiscordMessageSender {
    constructor(discordConnection, logger) {
        this.discordConnection = discordConnection;
        this.logger = logger;
    }

    send(channelId, embed) {
        return new Promise((resolve, reject) => {
            this.discordConnection.getConnectedClient().then((client) => {
                const channel = client.channels.cache.find((ch) => ch.id === channelId);

                if (!channel) {
                    this.logger.debug(
                        `Unable to Send Message: Client Can't Find Channel: '${channelId}'`,
                    );
                    return reject();
                }

                if (channel.type !== ChannelType.GuildText) {
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
                    this.logger.warn('Unable to Send Message: Channel Not Visibile', metadata);
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
            });
        });
    }
}

module.exports = DiscordMessageSender;
