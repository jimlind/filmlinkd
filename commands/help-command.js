class HelpCommand {
    /**
     * @param {import('../config')} config
     * @param {import('../services/discord/discord-connection')} discordConnection
     * @param {import('../services/google/firestore/firestore-user-dao')} firestoreUserDao
     * @param {import('../factories/message-embed-factory')} messageEmbedFactory
     * @param {import('../services/subscribed-user-list')} subscribedUserList
     */
    constructor(
        config,
        discordConnection,
        firestoreUserDao,
        messageEmbedFactory,
        subscribedUserList,
    ) {
        this.config = config;
        this.discordConnection = discordConnection;
        this.firestoreUserDao = firestoreUserDao;
        this.messageEmbedFactory = messageEmbedFactory;
        this.subscribedUserList = subscribedUserList;
    }

    /**
     * @returns {import('discord.js').MessageEmbed}
     */
    getMessage() {
        const getUserCount = this.firestoreUserDao.count();
        const getServerCount = this.discordConnection.getConnectedClient().then((client) => {
            if (client.shard === null) {
                return client.guilds.cache.size;
            }

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
                return this.messageEmbedFactory.createHelpMessage(this.config, 0, 0);
            });
    }
}

module.exports = HelpCommand;
