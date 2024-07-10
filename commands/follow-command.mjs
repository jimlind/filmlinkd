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
        // Store this promise as a variable for reuse
        const userPromise = this.getUserDataObjectFromAccountName(accountName);

        return userPromise
            .then((userModel) => {
                // If the user isn't found, throw an error to short circuit the catch
                if (!userModel) {
                    throw null;
                }

                // Update the channel subscriptions
                this.firestoreSubscriptionDao.subscribe(userModel, channelId);
                return userPromise;
            })
            .then((userModel) => {
                // Publish to Discord channel most recent entry for user
                const mostRecentPromise = this.diaryEntryProcessor.processMostRecentForUser(
                    userModel,
                    channelId,
                );
                return Promise.all([mostRecentPromise, this.pubSubConnection.getCommandTopic()]);
            })
            .then(([mostRecent, pubSubTopic]) => {
                // Publish to PubSub a message with details about follow command and results
                const userLid = mostRecent?.[0]?.userLid || '';
                const entryLid = mostRecent?.[0]?.entryLid || '';
                const data = { command: 'FOLLOW', user: userLid, entry: entryLid };
                const buffer = Buffer.from(JSON.stringify(data));
                pubSubTopic.publishMessage({ data: buffer });

                return userPromise;
            })
            .then((userModel) => {
                // Return the follow success message
                return this.embedBuilderFactory.createFollowSuccessEmbed(userModel);
            })
            .catch(() => this.embedBuilderFactory.createNoAccountFoundEmbed(accountName));
    }

    /**
     * Gets the LID from the web. Gets the user from the datastorage.
     * Creates a record if getByLetterboxdId returns something falsey.
     *
     * @param {string} accountName
     * @returns {Promise<Object>}
     */
    getUserDataObjectFromAccountName(accountName) {
        // Get the LID based on account name from the web because search is too vauge
        return this.letterboxdLidWeb
            .get(accountName)
            .then((lid) => this.letterboxdMemberApi.getMember(lid))
            .then((letterboxdMember) => {
                // If an existing database model exists update it or create something new
                return this.firestoreUserDao
                    .getByLetterboxdId(letterboxdMember.id)
                    .then((userModel) => {
                        if (userModel) {
                            // Update existing user model
                            return this.firestoreUserDao.updateByLetterboxdId(
                                letterboxdMember.id,
                                letterboxdMember.userName,
                                letterboxdMember.displayName,
                                letterboxdMember?.avatar?.getLargestImage(),
                            );
                        } else {
                            // Create new user model
                            return this.firestoreUserDao.create(
                                letterboxdMember.id,
                                letterboxdMember.userName,
                                letterboxdMember.displayName,
                                letterboxdMember?.avatar?.getLargestImage(),
                            );
                        }
                    });
            })
            .catch(() => null);
    }
}
