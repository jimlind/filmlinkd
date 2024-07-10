export default class InteractionTranslator {
    /**
     * @param {import('../commands/contributor-command.mjs')} contributorCommand
     * @param {import('../commands/diary-command.mjs')} diaryCommand
     * @param {import('./discord/discord-connection.mjs')} discordConnection
     * @param {import('discord.js')} discordLibrary
     * @param {import('./discord/discord-message-sender.mjs')} discordMessageSender
     * @param {any} embedBuilderFactory
     * @param {import('../commands/film-command.mjs')} filmCommand
     * @param {import('./google/firestore/firestore-subscription-dao.mjs')} firestoreSubscriptionDao
     * @param {import('./google/firestore/firestore-user-dao')} firestoreUserDao
     * @param {import('../commands/follow-command.mjs')} followCommand
     * @param {import('../commands/following-command.mjs')} followingCommand
     * @param {import('../commands/help-command.mjs')} helpCommand
     * @param {import('../commands/list-command.mjs')} listCommand
     * @param {import('../commands/logged-command.mjs')} loggedCommand
     * @param {import('../commands/refresh-command.mjs')} refreshCommand
     * @param {import('../commands/roulette-command.mjs')} rouletteCommand
     * @param {any} subscribedUserList
     * @param {import('../commands/test-command.mjs')} testCommand
     * @param {import('../commands/unfollow-command.mjs')} unfollowCommand
     * @param {import('../commands/user-command.mjs')} userCommand
     */
    constructor(
        contributorCommand,
        diaryCommand,
        discordConnection,
        discordLibrary,
        discordMessageSender,
        embedBuilderFactory,
        filmCommand,
        firestoreSubscriptionDao,
        firestoreUserDao,
        followCommand,
        followingCommand,
        helpCommand,
        listCommand,
        loggedCommand,
        refreshCommand,
        rouletteCommand,
        subscribedUserList,
        testCommand,
        unfollowCommand,
        userCommand,
    ) {
        this.contributorCommand = contributorCommand;
        this.diaryCommand = diaryCommand;
        this.discordConnection = discordConnection;
        this.discordLibrary = discordLibrary;
        this.discordMessageSender = discordMessageSender;
        this.embedBuilderFactory = embedBuilderFactory;
        this.filmCommand = filmCommand;
        this.firestoreSubscriptionDao = firestoreSubscriptionDao;
        this.firestoreUserDao = firestoreUserDao;
        this.followCommand = followCommand;
        this.followingCommand = followingCommand;
        this.helpCommand = helpCommand;
        this.listCommand = listCommand;
        this.loggedCommand = loggedCommand;
        this.refreshCommand = refreshCommand;
        this.rouletteCommand = rouletteCommand;
        this.subscribedUserList = subscribedUserList;
        this.testCommand = testCommand;
        this.unfollowCommand = unfollowCommand;
        this.userCommand = userCommand;
    }

    /**
     * @param {import('discord.js').CommandInteraction} commandInteraction
     */
    translate(commandInteraction) {
        this.getEmbedBuilderPromiseAfterNeccesaryAction(commandInteraction).then((result) => {
            if (result instanceof this.discordLibrary.EmbedBuilder) {
                return commandInteraction.editReply({ embeds: [result] });
            } else if (
                Array.isArray(result) &&
                result.every((r) => r instanceof this.discordLibrary.EmbedBuilder)
            ) {
                commandInteraction.editReply({ embeds: [result.shift()] });
                result.forEach((embed) =>
                    this.discordMessageSender
                        .send(commandInteraction.channelId, embed)
                        .then(() => true)
                        .catch(() => false),
                );
                return;
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

        // This is one of the few places in this code that I use an await.
        // It was mostly because I didn't want to wrap everything here in the `then` and it was
        // a fast way to get things done.
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
                return this.refreshCommand.process(accountName);
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
                // Respond with the "test" help responses
                if (commandInteraction.options.getBoolean('test') || false) {
                    return this.testCommand.process();
                }

                return this.helpCommand.getEmbed();
                break;
        }
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
