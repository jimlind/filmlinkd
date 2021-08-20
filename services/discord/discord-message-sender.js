'use strict';

class DiscordMessageSender {
    constructor(discordConnection, logger) {
        this.discordConnection = discordConnection;
        this.logger = logger;
    }

    send(channelId, message) {
        return new Promise((resolve, reject) => {
            this.discordConnection.getConnectedClient().then((client) => {
                const channel = client.channels.cache.find((ch) => ch.id === channelId);

                if (!channel) {
                    const metadata = {
                        channelId,
                        messageEmbed: message.toJSON(),
                    };
                    this.logger.warn('Unable to Send Message: Bad Channel Id', metadata);
                    return reject();
                }

                if (!channel.viewable) {
                    const metadata = {
                        channelId,
                        messageEmbed: message.toJSON(),
                    };
                    this.logger.warn('Unable to Send Message: Channel Not Visibile', metadata);
                    return reject();
                }

                channel
                    .send({ embeds: [message] })
                    .then(() => {
                        const metadata = {
                            channelId,
                            guild: channel.guild.name,
                            channel: channel.name,
                            messageEmbed: message.toJSON(),
                        };
                        this.logger.debug('Successfully Sent Message', metadata);
                        return resolve();
                    })
                    .catch(() => {
                        const metadata = {
                            channelId,
                            guild: channel.guild.name,
                            channel: channel.name,
                            messageEmbed: message.toJSON(),
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
