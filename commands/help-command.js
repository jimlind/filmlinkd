class HelpCommand {
    /**
     * @param {import('../models/config')} config
     * @param {import('../services/discord/discord-connection')} discordConnection
     * @param {import('../factories/message-embed-factory')} messageEmbedFactory
     * @param {import('../services/subscribed-user-list')} subscribedUserList
     */
    constructor(config, discordConnection, messageEmbedFactory, subscribedUserList) {
        this.config = config;
        this.discordConnection = discordConnection;
        this.messageEmbedFactory = messageEmbedFactory;
        this.subscribedUserList = subscribedUserList;
    }

    /**
     * @returns {import('discord.js').MessageEmbed}
     */
    getMessage() {
        const getClient = this.discordConnection.getConnectedClient();
        const getUserCount = getClient.then((client) => {
            return client.shard.fetchClientValues('filmLinkdUserCount').then((results) => {
                return results.reduce((acc, current) => acc + current, 0);
            });
        });
        const getServerCount = getClient.then((client) => {
            return client.shard.fetchClientValues('guilds.cache.size').then((results) => {
                return results.reduce((acc, current) => acc + current, 0);
            });
        });

        return Promise.all([getUserCount, getServerCount])
            .then(([userCount, serverCount]) => {
                return this.messageEmbedFactory.createHelpMessage(
                    this.config,
                    userCount,
                    serverCount,
                );
            })
            .catch(() => {
                const configData = { packageName: 'filmlinkd', packageVersion: '0.0.0' };
                return this.messageEmbedFactory.createHelpMessage(configData, 0, 0);
            });
    }
}

module.exports = HelpCommand;
