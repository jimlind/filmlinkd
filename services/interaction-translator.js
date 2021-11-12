'use strict';

class InteractionTranslator {
    /**
     * @param {import('./discord/discord-connection')} discordConnection
     * @param {import('./diary-entry/diary-entry-processor')} diaryEntryProcessor
     * @param {import('./google/firestore/firestore-subscription-dao')} firestoreSubscriptionDao
     * @param {import('./google/firestore/firestore-user-dao')} firestoreUserDao
     * @param {any} messageEmbedFactory
     * @param {any} letterboxdProfileWeb
     * @param {import('./letterboxd/letterboxd-letterboxd-id-web')} letterboxdLetterboxdIdWeb
     * @param {any} subscribedUserList
     */
    constructor(
        discordConnection,
        diaryEntryProcessor,
        firestoreSubscriptionDao,
        firestoreUserDao,
        messageEmbedFactory,
        letterboxdProfileWeb,
        letterboxdLetterboxdIdWeb,
        subscribedUserList,
    ) {
        this.discordConnection = discordConnection;
        this.diaryEntryProcessor = diaryEntryProcessor;
        this.firestoreSubscriptionDao = firestoreSubscriptionDao;
        this.firestoreUserDao = firestoreUserDao;
        this.messageEmbedFactory = messageEmbedFactory;
        this.letterboxdProfileWeb = letterboxdProfileWeb;
        this.letterboxdLetterboxdIdWeb = letterboxdLetterboxdIdWeb;
        this.subscribedUserList = subscribedUserList;
    }

    /**
     * @param {import('discord.js').CommandInteraction} commandInteraction
     */
    translate(commandInteraction) {
        this.getMessagePromiseAfterNeccesaryAction(commandInteraction).then((message) => {
            if (message instanceof require('discord.js').MessageEmbed) {
                return commandInteraction.editReply({ embeds: [message] });
            } else {
                return commandInteraction.editReply(
                    'Something has gone terribly wrong. Please enter a bug report',
                );
            }
        });
    }

    /**
     * @param {import('discord.js').CommandInteraction} commandInteraction
     * @returns {Promise<import('discord.js').MessageEmbed>}
     */
    async getMessagePromiseAfterNeccesaryAction(commandInteraction) {
        // Check for restricted commands and return the appropriate message
        if (this.commandIsRestricted(commandInteraction)) {
            return new Promise((resolve) => {
                return resolve(this.messageEmbedFactory.createInadequatePermissionsMessage());
            });
        }

        const accountName = (commandInteraction.options.getString('account') || '').toLowerCase();
        const channelId = await this.getChannelId(commandInteraction);
        if (!channelId) {
            return this.messageEmbedFactory.createChannelNotFoundMessage();
        }

        switch (commandInteraction.commandName) {
            case 'follow':
                return this.followAccount(accountName, channelId)
                    .then((userData) => {
                        return this.messageEmbedFactory.createFollowSuccessMessage(userData);
                    })
                    .catch(() => {
                        return this.messageEmbedFactory.createNoAccountFoundMessage(accountName);
                    });
                break;
            case 'following':
                return this.firestoreSubscriptionDao.list(channelId).then((userList) => {
                    if (userList.length) {
                        return this.messageEmbedFactory.createFollowingMessage(userList);
                    } else {
                        return this.messageEmbedFactory.createEmptyFollowingMessage();
                    }
                });
                break;
            case 'refresh':
                return this.refreshAccount(accountName)
                    .then((userData) => {
                        return this.messageEmbedFactory.createRefreshSuccessMessage(userData);
                    })
                    .catch(() => {
                        return this.messageEmbedFactory.createRefreshErrorMessage(accountName);
                    });
                break;
            case 'unfollow':
                return this.unfollowAccount(accountName, channelId)
                    .then((userData) => {
                        return this.messageEmbedFactory.createUnfollowedSuccessMessage(userData);
                    })
                    .catch(() => {
                        return this.messageEmbedFactory.createUnfollowedErrorMessage(accountName);
                    });
                break;
            default:
                return new Promise((resolve) => {
                    return resolve(this.messageEmbedFactory.createHelpMessage());
                });
                break;
        }
    }

    /**
     * @param {import('discord.js').CommandInteraction} interaction
     * @returns boolean
     */
    commandIsRestricted(interaction) {
        // If command is not in the protected command list is restricted
        const protectedCommands = ['follow', 'unfollow'];
        if (!protectedCommands.includes(interaction.commandName)) {
            return false;
        }

        // If member is not a normal guild member command is not restricted
        if (!(interaction.member instanceof require('discord.js').GuildMember)) {
            return true;
        }

        // Nothing is restricted for guild managers
        return !interaction.member.permissions.has('MANAGE_GUILD');
    }

    /**
     * @param {string} accountName
     * @param {string} channelId
     * @returns {Promise<import('../models/user')>}
     */
    followAccount(accountName, channelId) {
        return this.getUserDataObjectFromAccountName(accountName)
            .then((userData) => {
                return this.firestoreSubscriptionDao.subscribe(userData, channelId);
            })
            .then((result) => {
                // TODO: If I spam following somebody this doesn't repeat. Why?
                this.diaryEntryProcessor.processMostRecentForUser(accountName, channelId);
                return result.userData;
            });
    }

    /**
     * @param {string} accountName
     * @returns {Promise<import('../models/user')>}
     */
    refreshAccount(accountName) {
        return this.letterboxdProfileWeb
            .get(accountName)
            .then((profile) => {
                return this.firestoreUserDao.update(accountName, profile.name, profile.image);
            })
            .then((userData) => {
                return userData;
            });
    }

    /**
     * @param {string} accountName
     * @param {string} channelId
     * @returns {Promise<import('../models/user')>}
     */
    unfollowAccount(accountName, channelId) {
        return this.firestoreSubscriptionDao
            .unsubscribe(accountName, channelId)
            .then((userData) => {
                // If the account no longer has any channels remove it from local cache
                if (!userData?.channelList?.length) {
                    this.subscribedUserList.remove(userData.userName);
                }
                return userData;
            });
    }

    /**
     * @param {string} accountName
     * @returns {Promise<import('../models/user')>}
     */
    getUserDataObjectFromAccountName(accountName) {
        return (
            this.firestoreUserDao
                .read(accountName)
                // User found so resolve on that
                .then((userData) => userData)
                // User not found so create a new user from thier Letterboxd profile
                .catch(() => {
                    const promiseList = [
                        this.letterboxdLetterboxdIdWeb.get(accountName),
                        this.letterboxdProfileWeb.get(accountName),
                    ];

                    return Promise.all(promiseList)
                        .then(([letterboxdId, profile]) => {
                            return this.firestoreUserDao.create(
                                letterboxdId,
                                accountName,
                                profile.name,
                                profile.image,
                            );
                        })
                        .then((userData) => userData);
                })
        );
    }

    /**
     * @param {import('discord.js').CommandInteraction} commandInteraction
     * @returns string
     */
    async getChannelId(commandInteraction) {
        const channelInput = commandInteraction.options.getString('channel') || '';
        if (!channelInput) {
            return commandInteraction.channelId;
        }

        const client = await this.discordConnection.getConnectedClient();
        return client.channels.cache.reduce((accumulator, current) => {
            if (current?.name === channelInput) {
                return current.id;
            }
            return accumulator;
        }, '');
    }
}

module.exports = InteractionTranslator;
