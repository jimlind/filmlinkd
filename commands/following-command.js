'use strict';

class FollowingCommand {
    /**
     * @param {import('../services/google/firestore/firestore-subscription-dao')} firestoreSubscriptionDao
     * @param {import('../factories/message-embed-factory')} messageEmbedFactory
     */
    constructor(firestoreSubscriptionDao, messageEmbedFactory) {
        this.firestoreSubscriptionDao = firestoreSubscriptionDao;
        this.messageEmbedFactory = messageEmbedFactory;
    }

    /**
     * @param {string} channelId
     * @returns {import('discord.js').MessageEmbed}
     */
    process(channelId) {
        return this.firestoreSubscriptionDao.list(channelId).then((userList) => {
            if (userList.length) {
                return this.messageEmbedFactory.createFollowingMessage(userList);
            } else {
                return this.messageEmbedFactory.createEmptyFollowingMessage();
            }
        });
    }
}

module.exports = FollowingCommand;
