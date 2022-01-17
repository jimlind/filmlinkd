class EntryCommand {
    /**
     * @param {import('../services/letterboxd/api/letterboxd-log-entry-api')} letterboxdLogEntryApi
     * @param {import('../services/letterboxd/api/letterboxd-film-api')} letterboxdFilmApi
     * @param {import('../services/letterboxd/letterboxd-lid-web')} letterboxdLidWeb
     * @param {import('../factories/message-embed-factory')} messageEmbedFactory
     */
    constructor(letterboxdFilmApi, letterboxdLidWeb, letterboxdLogEntryApi, messageEmbedFactory) {
        this.letterboxdFilmApi = letterboxdFilmApi;
        this.letterboxdLidWeb = letterboxdLidWeb;
        this.letterboxdLogEntryApi = letterboxdLogEntryApi;
        this.messageEmbedFactory = messageEmbedFactory;
    }

    /**
     * @param {string} accountName
     * @param {string} filmName
     * @returns {import('discord.js').MessageEmbed}
     */
    getMessage(accountName, filmName) {
        const promiseList = [
            this.letterboxdLidWeb.get(accountName),
            this.letterboxdFilmApi.search(filmName),
        ];

        return Promise.all(promiseList)
            .then(([memberLetterboxdId, filmLetterboxdId]) => {
                return this.letterboxdLogEntryApi.getByMemberAndFilm(
                    memberLetterboxdId,
                    filmLetterboxdId,
                    5,
                );
            })
            .then((logEntryList) => {
                if (!logEntryList.length) {
                    throw 'Empty Entry List Found';
                }

                return this.messageEmbedFactory.createLoggedMessage(logEntryList);
            })
            .catch((e) => {
                console.log(e);
                return this.messageEmbedFactory.createNoLoggedEntriesFoundMessage();
            });
    }
}

module.exports = EntryCommand;
