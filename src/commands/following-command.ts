'use strict';

export default class FollowingCommand {
    /**
     * @param {import('../services/google/firestore/firestore-subscription-dao.mjs')} firestoreSubscriptionDao
     * @param {import('../factories/embed-builder-factory.mjs')} embedBuilderFactory
     */
    constructor(readonly firestoreSubscriptionDao: any, readonly embedBuilderFactory: any) {
        this.firestoreSubscriptionDao = firestoreSubscriptionDao;
        this.embedBuilderFactory = embedBuilderFactory;
    }

    /**
     * @param {string} channelId
     * @returns {import('discord.js').EmbedBuilder}
     */
    process(channelId: any) {
        return this.firestoreSubscriptionDao.list(channelId).then((userList: any) => {
            if (userList.length) {
                return this.embedBuilderFactory.createFollowingEmbedList(userList);
            } else {
                return this.embedBuilderFactory.createEmptyFollowingEmbed();
            }
        });
    }
}
