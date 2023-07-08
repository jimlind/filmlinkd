export default class InteractionTranslator {
    /**
     * @param {import('../commands/contributor-command')} contributorCommand
     * @param {import('../commands/diary-command')} diaryCommand
     * @param {import('./discord/discord-connection')} discordConnection
     * @param {any} embedBuilderFactory
     * @param {import('../commands/film-command')} filmCommand
     * @param {import('./google/firestore/firestore-subscription-dao')} firestoreSubscriptionDao
     * @param {import('./google/firestore/firestore-user-dao')} firestoreUserDao
     * @param {import('../commands/follow-command')} followCommand
     * @param {import('../commands/following-command')} followingCommand
     * @param {import('../commands/help-command')} helpCommand
     * @param {any} letterboxdProfileWeb
     * @param {import('../commands/list-command')} listCommand
     * @param {import('../commands/logged-command')} loggedCommand
     * @param {import('../commands/roulette-command')} rouletteCommand
     * @param {any} subscribedUserList
     * @param {import('../commands/unfollow-command')} unfollowCommand
     * @param {import('../commands/user-command')} userCommand
     */
    constructor(
        contributorCommand,
        diaryCommand,
        discordConnection,
        embedBuilderFactory,
        filmCommand,
        firestoreSubscriptionDao,
        firestoreUserDao,
        followCommand,
        followingCommand,
        helpCommand,
        letterboxdProfileWeb,
        listCommand,
        loggedCommand,
        rouletteCommand,
        subscribedUserList,
        unfollowCommand,
        userCommand,
    ) {
        this.contributorCommand = contributorCommand;
        this.diaryCommand = diaryCommand;
        this.discordConnection = discordConnection;
        this.embedBuilderFactory = embedBuilderFactory;
        this.filmCommand = filmCommand;
        this.firestoreSubscriptionDao = firestoreSubscriptionDao;
        this.firestoreUserDao = firestoreUserDao;
        this.followCommand = followCommand;
        this.followingCommand = followingCommand;
        this.helpCommand = helpCommand;
        this.letterboxdProfileWeb = letterboxdProfileWeb;
        this.listCommand = listCommand;
        this.loggedCommand = loggedCommand;
        this.rouletteCommand = rouletteCommand;
        this.subscribedUserList = subscribedUserList;
        this.unfollowCommand = unfollowCommand;
        this.userCommand = userCommand;
    }

    /**
     * @param {import('discord.js').CommandInteraction} commandInteraction
     */
    translate(commandInteraction) {
        this.getEmbedBuilderPromiseAfterNeccesaryAction(commandInteraction).then((embedBuilder) => {
            if (embedBuilder instanceof require('discord.js').EmbedBuilder) {
                //There is a 4096 character limit on descriptions so cut things off to keep the bot happy
                const description = embedBuilder?.data?.description || '';
                embedBuilder.setDescription(description.substring(0, 4096));

                return commandInteraction.editReply({ embeds: [embedBuilder] });
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
     * @returns {Promise<import('discord.js').EmbedBuilder>}
     */
    async getEmbedBuilderPromiseAfterNeccesaryAction(commandInteraction) {
        const accountName = (commandInteraction.options.getString('account') || '').toLowerCase();
        const filmName = commandInteraction.options.getString('film-name') || '';

        const channelId = await this.getChannelId(commandInteraction);
        if (!channelId) {
            return this.embedBuilderFactory.createChannelNotFoundEmbed();
        }

        switch (commandInteraction.commandName) {
            case 'follow':
                return this.followCommand.process(accountName, channelId);
                break;
            case 'following':
                return this.followingCommand.process(channelId);
                break;
            case 'refresh':
                return this.refreshAccount(accountName)
                    .then((userData) => {
                        return this.embedBuilderFactory.createRefreshSuccessEmbed(userData);
                    })
                    .catch(() => {
                        return this.embedBuilderFactory.createRefreshErrorEmbed(accountName);
                    });
                break;
            case 'unfollow':
                return this.unfollowCommand.process(accountName, channelId);
                break;
            case 'contributor':
                const contributorName =
                    commandInteraction.options.getString('contributor-name') || '';
                return this.contributorCommand.getEmbed(contributorName);
                break;
            case 'diary':
                return this.diaryCommand.getEmbed(accountName);
                break;
            case 'film':
                return this.filmCommand.getEmbed(filmName);
                break;
            case 'list':
                const listName = commandInteraction.options.getString('list-name') || '';
                return this.listCommand.getEmbed(accountName, listName);
                break;
            case 'logged':
                return this.loggedCommand.getEmbed(accountName, filmName);
                break;
            case 'roulette':
                return this.rouletteCommand.getEmbed();
                break;
            case 'user':
                return this.userCommand.getEmbed(accountName);
                break;
            default:
                return this.helpCommand.getEmbed();
                break;
        }
    }

    /**
     * @param {string} accountName
     * @returns {Promise<import('../models/user')>}
     */
    refreshAccount(accountName) {
        return this.letterboxdProfileWeb.get(accountName).then((profile) => {
            return this.firestoreUserDao.update(accountName, profile.name, profile.image);
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
