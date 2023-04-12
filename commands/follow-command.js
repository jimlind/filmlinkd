'use strict';

class FollowCommand {
    /**
     * @param {import('../services/diary-entry/diary-entry-processor')} diaryEntryProcessor
     * @param {import('../services/google/firestore/firestore-subscription-dao')} firestoreSubscriptionDao
     * @param {import('../services/google/firestore/firestore-user-dao')} firestoreUserDao
     * @param {import('../services/letterboxd/letterboxd-lid-web')} letterboxdLidWeb
     * @param {import('../services/letterboxd/api/letterboxd-member-api')} letterboxdMemberApi
     * @param {import('../factories/embed-builder-factory')} embedBuilderFactory
     * @param {import('../services/logger')} logger
     */
    constructor(
        diaryEntryProcessor,
        firestoreSubscriptionDao,
        firestoreUserDao,
        letterboxdLidWeb,
        letterboxdMemberApi,
        embedBuilderFactory,
        logger,
    ) {
        this.diaryEntryProcessor = diaryEntryProcessor;
        this.firestoreSubscriptionDao = firestoreSubscriptionDao;
        this.firestoreUserDao = firestoreUserDao;
        this.letterboxdLidWeb = letterboxdLidWeb;
        this.letterboxdMemberApi = letterboxdMemberApi;
        this.embedBuilderFactory = embedBuilderFactory;
        this.logger = logger;
    }

    /**
     * @param {string} accountName
     * @param {string} channelId
     * @returns {import('discord.js').EmbedBuilder}
     */
    process(accountName, channelId) {
        const userPromise = this.getUserDataObjectFromAccountName(accountName);
        const promiseList = [
            userPromise.then((data) => this.firestoreSubscriptionDao.subscribe(data, channelId)),
            userPromise.then((data) => this.embedBuilderFactory.createFollowSuccessEmbed(data)),
            userPromise.then((data) =>
                this.diaryEntryProcessor.processMostRecentForUser(data, channelId),
            ),
        ];

        return Promise.all(promiseList)
            .then(([subscribeResult, messageResult, mostRecentResult]) => messageResult)
            .catch(() => this.embedBuilderFactory.createNoAccountFoundEmbed(accountName));
    }

    /**
     * Gets the user from the datastorage. Creates a record if getByUserName fails.
     *
     * @param {string} accountName
     * @returns {Promise<Object>}
     */
    getUserDataObjectFromAccountName(accountName) {
        return this.firestoreUserDao
            .getByUserName(accountName)
            .then((userData) => userData)
            .catch(() => {
                return this.letterboxdLidWeb
                    .get(accountName)
                    .then((lid) => {
                        return this.letterboxdMemberApi.getMember(lid);
                    })
                    .then((letterboxdMember) => {
                        return this.firestoreUserDao.create(
                            letterboxdMember.id,
                            letterboxdMember.userName,
                            letterboxdMember.displayName,
                            this.parseImage(letterboxdMember?.avatar?.sizes),
                        );
                    });
            });
    }

    /**
     * @param {import('../models/letterboxd/letterboxd-image-size')[]} sizes
     * return string
     */
    parseImage(sizes) {
        const findLargest = (previous, current) =>
            current.height || 0 > previous.height ? current : previous;
        const largestImage = (sizes || []).reduce(findLargest, {});
        return largestImage?.url || '';
    }
}

module.exports = FollowCommand;
