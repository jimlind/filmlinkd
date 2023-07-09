export default class UserCommand {
    /**
     * @param {import('../services/google/firestore/firestore-subscription-dao')} firestoreSubscriptionDao
     * @param {import('../factories/embed-builder-factory.mjs')} embedBuilderFactory
     */
    constructor(firestoreSubscriptionDao, embedBuilderFactory) {
        this.firestoreSubscriptionDao = firestoreSubscriptionDao;
        this.embedBuilderFactory = embedBuilderFactory;
    }

    /**
     * Remove the record from the database and create a message.
     * Don't worry about the local cache of users because it will get reset when the bot restart
     *
     * @param {string} accountName
     * @param {string} channelId
     * @returns {import('discord.js').EmbedBuilder}
     */
    process(accountName, channelId) {
        return this.firestoreSubscriptionDao
            .unsubscribe(accountName, channelId)
            .then((userData) => {
                return this.embedBuilderFactory.createUnfollowedSuccessEmbed(userData);
            })
            .catch(() => {
                return this.embedBuilderFactory.createUnfollowedErrorEmbed(accountName);
            });
    }
}
