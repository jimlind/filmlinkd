'use strict';

export default class FollowCommand {
    /**
     * @param {import('../services/diary-entry/diary-entry-processor.mjs')} diaryEntryProcessor
     * @param {import('../factories/embed-builder-factory.mjs')} embedBuilderFactory
     * @param {import('../services/google/firestore/firestore-subscription-dao.mjs')} firestoreSubscriptionDao
     * @param {import('../services/google/firestore/firestore-user-dao.mjs')} firestoreUserDao
     * @param {import('../services/letterboxd/letterboxd-lid-web.mjs')} letterboxdLidWeb
     * @param {import('../services/letterboxd/api/letterboxd-member-api.mjs')} letterboxdMemberApi
     * @param {import('../services/google/pubsub/pub-sub-connection.mjs')} pubSubConnection
     */
    constructor(
        diaryEntryProcessor,
        embedBuilderFactory,
        firestoreSubscriptionDao,
        firestoreUserDao,
        letterboxdLidWeb,
        letterboxdMemberApi,
        pubSubConnection,
    ) {
        this.diaryEntryProcessor = diaryEntryProcessor;
        this.embedBuilderFactory = embedBuilderFactory;
        this.firestoreSubscriptionDao = firestoreSubscriptionDao;
        this.firestoreUserDao = firestoreUserDao;
        this.letterboxdLidWeb = letterboxdLidWeb;
        this.letterboxdMemberApi = letterboxdMemberApi;
        this.pubSubConnection = pubSubConnection;
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
            this.pubSubConnection.getCommandTopic(),
        ];

        return Promise.all(promiseList)
            .then(([subscribeResult, messageResult, mostRecentResult, pubSubTopic]) => {
                // Publish needed information about the command for pickup by other systems
                const result = mostRecentResult[0];
                const data = { command: 'FOLLOW', user: result.userLid, entry: result.entryLid };
                const buffer = Buffer.from(JSON.stringify(data));
                pubSubTopic.publishMessage({ data: buffer });

                // Return embed for writing to Discord
                return messageResult;
            })
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
     * @param {import('../models/letterboxd/letterboxd-image-size.mjs')[]} sizes
     * return string
     */
    parseImage(sizes) {
        const findLargest = (previous, current) =>
            current.height || 0 > previous.height ? current : previous;
        const largestImage = (sizes || []).reduce(findLargest, {});
        return largestImage?.url || '';
    }
}
