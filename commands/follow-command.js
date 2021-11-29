'use strict';

class FollowCommand {
    /**
     * @param {import('../services/diary-entry/diary-entry-processor')} diaryEntryProcessor
     * @param {import('../services/google/firestore/firestore-subscription-dao')} firestoreSubscriptionDao
     * @param {import('../services/google/firestore/firestore-user-dao')} firestoreUserDao
     * @param {import('../services/letterboxd/api/letterboxd-member-api')} letterboxdMemberApi
     * @param {import('../factories/message-embed-factory')} messageEmbedFactory
     */
    constructor(
        diaryEntryProcessor,
        firestoreSubscriptionDao,
        firestoreUserDao,
        letterboxdMemberApi,
        messageEmbedFactory,
    ) {
        this.diaryEntryProcessor = diaryEntryProcessor;
        this.firestoreSubscriptionDao = firestoreSubscriptionDao;
        this.firestoreUserDao = firestoreUserDao;
        this.letterboxdMemberApi = letterboxdMemberApi;
        this.messageEmbedFactory = messageEmbedFactory;
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
            .then(([subscribeResult, messageResult, mostRecentResult]) => messageResult)
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
