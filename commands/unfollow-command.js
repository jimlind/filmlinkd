class UserCommand {
    /**
     * @param {import('../services/google/firestore/firestore-subscription-dao')} firestoreSubscriptionDao
     * @param {import('../factories/message-embed-factory')} messageEmbedFactory
     */
    constructor(firestoreSubscriptionDao, messageEmbedFactory) {
        this.firestoreSubscriptionDao = firestoreSubscriptionDao;
        this.messageEmbedFactory = messageEmbedFactory;
    }

    /**
     * Remove the record from the database and create a message.
     * Don't worry about the local cache of users because it will get reset when the bot restart
     *
     * @param {string} accountName
     * @param {string} channelId
     * @returns {import('discord.js').MessageEmbed}
     */
    process(accountName, channelId) {
        return this.firestoreSubscriptionDao
            .unsubscribe(accountName, channelId)
            .then((userData) => {
                return this.messageEmbedFactory.createUnfollowedSuccessMessage(userData);
            })
            .catch(() => {
                return this.messageEmbedFactory.createUnfollowedErrorMessage(accountName);
            });
    }
}

module.exports = UserCommand;
