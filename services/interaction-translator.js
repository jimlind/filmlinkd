'use strict';

class InteractionTranslator {
    /**
     * @param {import('../commands/contributor-command')} contributorCommand
     * @param {import('../commands/diary-command')} diaryCommand
     * @param {import('./discord/discord-connection')} discordConnection
     * @param {import('../commands/film-command')} filmCommand
     * @param {import('./google/firestore/firestore-subscription-dao')} firestoreSubscriptionDao
     * @param {import('./google/firestore/firestore-user-dao')} firestoreUserDao
     * @param {import('../commands/follow-command')} followCommand
     * @param {any} messageEmbedFactory
     * @param {any} letterboxdProfileWeb
     * @param {any} subscribedUserList
     */
    constructor(
        contributorCommand,
        diaryCommand,
        discordConnection,
        filmCommand,
        firestoreSubscriptionDao,
        firestoreUserDao,
        followCommand,
        messageEmbedFactory,
        letterboxdProfileWeb,
        subscribedUserList,
    ) {
        this.contributorCommand = contributorCommand;
        this.diaryCommand = diaryCommand;
        this.discordConnection = discordConnection;
        this.filmCommand = filmCommand;
        this.firestoreSubscriptionDao = firestoreSubscriptionDao;
        this.firestoreUserDao = firestoreUserDao;
        this.followCommand = followCommand;
        this.messageEmbedFactory = messageEmbedFactory;
        this.letterboxdProfileWeb = letterboxdProfileWeb;
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
     * I already regret what I named this method, but still can't come up with something better
     *
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
                return this.followCommand.process(accountName, channelId);
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
            case 'diary':
                return this.diaryCommand.getMessage(accountName);
            case 'film':
                const filmName = commandInteraction.options.getString('film-name') || '';
                return this.filmCommand.getMessage(filmName);
            case 'contributor':
                const contributorName =
                    commandInteraction.options.getString('contributor-name') || '';
                return this.contributorCommand.getMessage(contributorName);
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
            if (current?.name === channelInput && current?.guildId === commandInteraction.guildId) {
                return current.id;
            }
            return accumulator;
        }, '');
    }
}

module.exports = InteractionTranslator;
