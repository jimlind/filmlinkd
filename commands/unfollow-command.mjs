export default class UserCommand {
    /**
     * @param {import('../services/google/firestore/firestore-subscription-dao.mjs')} firestoreSubscriptionDao
     * @param {import('../services/google/firestore/firestore-user-dao.mjs')} firestoreUserDao
     * @param {import('../factories/embed-builder-factory.mjs')} embedBuilderFactory
     * @param {import('../services/letterboxd/letterboxd-lid-web.mjs')} letterboxdLidWeb
     */
    constructor(firestoreSubscriptionDao, firestoreUserDao, embedBuilderFactory, letterboxdLidWeb) {
        this.firestoreSubscriptionDao = firestoreSubscriptionDao;
        this.firestoreUserDao = firestoreUserDao;
        this.embedBuilderFactory = embedBuilderFactory;
        this.letterboxdLidWeb = letterboxdLidWeb;
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
        // Get the LID based on account name from the web because search is too vauge
        const userPromise = this.letterboxdLidWeb
            .get(accountName)
            .then((lid) => this.firestoreUserDao.getByLetterboxdId(lid));

        return userPromise
            .then((userModel) => {
                // If the user isn't found, throw an error to short circuit the catch
                if (!userModel) {
                    throw null;
                }

                this.firestoreSubscriptionDao.unsubscribe(userModel, channelId);
                return userPromise;
            })
            .then((userModel) => this.embedBuilderFactory.createUnfollowedSuccessEmbed(userModel))
            .catch(() => this.embedBuilderFactory.createUnfollowedErrorEmbed(accountName));
    }
}
