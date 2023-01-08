class RouletteCommand {
    /**
     * @param {import('../commands/film-command')} filmCommand
     * @param {import('../services/letterboxd/api/letterboxd-entry-api')}letterboxdEntryApi
     * @param {import('../services/letterboxd/letterboxd-lid-web')} letterboxdLidWeb
     * @param {import('../factories/message-embed-factory')} messageEmbedFactory
     * @param {import('../services/subscribed-user-list')} subscribedUserList
     */
    constructor(
        filmCommand,
        letterboxdEntryApi,
        letterboxdLidWeb,
        messageEmbedFactory,
        subscribedUserList,
    ) {
        this.filmCommand = filmCommand;
        this.letterboxdEntryApi = letterboxdEntryApi;
        this.letterboxdLidWeb = letterboxdLidWeb;
        this.messageEmbedFactory = messageEmbedFactory;
        this.subscribedUserList = subscribedUserList;
    }

    /**
     * @returns {import('discord.js').MessageEmbed}
     */
    getMessage() {
        return this.subscribedUserList
            .getAllActiveSubscriptions()
            .then((subscriberList) => {
                const index = Math.floor(Math.random() * subscriberList.length);
                return subscriberList[index]?.userName;
            })
            .then((userName) => this.letterboxdLidWeb.get(userName))
            .then((memberLetterboxdId) => this.letterboxdEntryApi.get(memberLetterboxdId, 10))
            .then((entryList) => {
                const index = Math.floor(Math.random() * entryList.length);
                return entryList[index]?.filmName;
            })
            .then((filmName) => {
                return this.filmCommand.getMessage(filmName);
            })
            .catch(() => this.messageEmbedFactory.createFilmNotFoundMessage());
    }
}

module.exports = RouletteCommand;
