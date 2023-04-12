'use strict';

class FollowingCommand {
    /**
     * @param {import('../services/google/firestore/firestore-subscription-dao')} firestoreSubscriptionDao
     * @param {import('../factories/embed-builder-factory')} embedBuilderFactory
     */
    constructor(firestoreSubscriptionDao, embedBuilderFactory) {
        this.firestoreSubscriptionDao = firestoreSubscriptionDao;
        this.embedBuilderFactory = embedBuilderFactory;
    }

    /**
     * @param {string} channelId
     * @returns {import('discord.js').EmbedBuilder}
     */
    process(channelId) {
        return this.firestoreSubscriptionDao.list(channelId).then((userList) => {
            if (userList.length) {
                return this.embedBuilderFactory.createFollowingEmbed(userList);
            } else {
                return this.embedBuilderFactory.createEmptyFollowingEmbed();
            }
        });
    }
}

module.exports = FollowingCommand;
