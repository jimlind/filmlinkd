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
        const promiseList = [
            this.discordConnection.getConnectedClient(),
            this.subscribedUserList.getAllActiveSubscriptions(),
        ];

        return Promise.all(promiseList)
            .then(([discordClient, subscriptionList]) => {
                const serverCount = discordClient.guilds.cache.size;
                const userCount = subscriptionList.length;

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
