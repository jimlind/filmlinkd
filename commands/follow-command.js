'use strict';

class FollowCommand {
    /**
     * @param {import('../services/diary-entry/diary-entry-processor')} diaryEntryProcessor
     * @param {import('../services/google/firestore/firestore-subscription-dao')} firestoreSubscriptionDao
     * @param {import('../services/google/firestore/firestore-user-dao')} firestoreUserDao
     * @param {import('../services/letterboxd/api/letterboxd-member-api')} letterboxdMemberApi
     * @param {import('../factories/message-embed-factory')} messageEmbedFactory
     * @param {import('../services/logger')} logger
     */
    constructor(
        diaryEntryProcessor,
        firestoreSubscriptionDao,
        firestoreUserDao,
        letterboxdMemberApi,
        messageEmbedFactory,
        logger,
    ) {
        this.diaryEntryProcessor = diaryEntryProcessor;
        this.firestoreSubscriptionDao = firestoreSubscriptionDao;
        this.firestoreUserDao = firestoreUserDao;
        this.letterboxdMemberApi = letterboxdMemberApi;
        this.messageEmbedFactory = messageEmbedFactory;
        this.logger = logger;
    }

    /**
     * @param {string} accountName
     * @param {string} channelId
     * @returns {import('discord.js').MessageEmbed}
     */
    process(accountName, channelId) {
        const userPromise = this.getUserDataObjectFromAccountName(accountName);
        const promiseList = [
            userPromise.then((data) => this.firestoreSubscriptionDao.subscribe(data, channelId)),
            userPromise.then((data) => this.messageEmbedFactory.createFollowSuccessMessage(data)),
            this.diaryEntryProcessor.processMostRecentForUser(accountName, channelId),
        ];

        return Promise.all(promiseList)
            .then(([subscribeResult, messageResult, mostRecentResult]) => {
                const userResult = subscribeResult.userData;
                const hourAgo = Date.now() - 60 * 60 * 1000;

                // Attempting to debug some issue with follow nullifying existing data
                // If user is subscribed to one channel and was created before an hour ago
                if (userResult.channelList.length === 1 && userResult.created < hourAgo) {
                    userPromise.then((data) => {
                        this.logger.info('Existing User Has One Subscribed Channel', {
                            original: data,
                            updated: subscribeResult.userData,
                        });
                    });
                }

                return messageResult;
            })
            .catch(() => this.messageEmbedFactory.createNoAccountFoundMessage(accountName));
    }

    /**
     * Gets the user from the datastorage. Creates a record getting fails.
     *
     * @param {string} accountName
     * @returns {Promise<Object>}
     */
    getUserDataObjectFromAccountName(accountName) {
        return this.firestoreUserDao
            .getByUserName(accountName)
            .then((userData) => userData)
            .catch(() => {
                return this.letterboxdMemberApi.search(accountName).then((letterboxdMember) => {
                    return this.firestoreUserDao.create(
                        letterboxdMember.id,
                        letterboxdMember.userName,
                        letterboxdMember.displayName,
                        letterboxdMember.image,
                    );
                });
            });
    }
}

module.exports = FollowCommand;
