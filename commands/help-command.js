class HelpCommand {
    /**
     * @param {import('../config')} config
     * @param {import('../services/discord/discord-connection')} discordConnection
     * @param {import('../services/google/firestore/firestore-user-dao')} firestoreUserDao
     * @param {import('../factories/embed-builder-factory')} embedBuilderFactory
     * @param {import('../services/subscribed-user-list')} subscribedUserList
     */
    constructor(
        config,
        discordConnection,
        firestoreUserDao,
        embedBuilderFactory,
        subscribedUserList,
    ) {
        this.config = config;
        this.discordConnection = discordConnection;
        this.firestoreUserDao = firestoreUserDao;
        this.embedBuilderFactory = embedBuilderFactory;
        this.subscribedUserList = subscribedUserList;
    }

    /**
     * @returns {import('discord.js').EmbedBuilder}
     */
    getEmbed() {
        const getUserCount = this.firestoreUserDao.count();
        const getServerCount = this.discordConnection.getConnectedClient().then((client) => {
            if (!client.cluster) {
                return client.guilds.cache.size;
            }

            return client.cluster.broadcastEval(`this.guilds.cache.size`).then((results) => {
                return results.reduce((acc, current) => acc + current, 0);
            });
        });

        return Promise.all([getUserCount, getServerCount])
            .then(([userCount, serverCount]) => {
                return this.embedBuilderFactory.createHelpEmbed(
                    this.config,
                    userCount,
                    serverCount,
                );
            })
            .catch(() => {
                return this.embedBuilderFactory.createHelpEmbed(this.config, 0, 0);
            });
    }
}

module.exports = HelpCommand;
