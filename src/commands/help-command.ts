export default class HelpCommand {
    /**
     * @param {import('../config.mjs')} config
     * @param {import('../services/discord/discord-connection.mjs')} discordConnection
     * @param {import('../services/google/firestore/firestore-user-dao.mjs')} firestoreUserDao
     * @param {import('../factories/embed-builder-factory.mjs')} embedBuilderFactory
     */
    constructor(
        readonly config: any,
        readonly discordConnection: any,
        readonly firestoreUserDao: any,
        readonly embedBuilderFactory: any,
    ) {}

    /**
     * @returns {import('discord.js').EmbedBuilder}
     */
    getEmbed() {
        const getUserCount = this.firestoreUserDao.count();
        const getServerCount = this.discordConnection.getConnectedClient().then((client: any) => {
            if (!client.cluster) {
                return client.guilds.cache.size;
            }

            return client.cluster.broadcastEval(`this.guilds.cache.size`).then((results: any) => {
                return results.reduce((acc: any, current: any) => acc + current, 0);
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
