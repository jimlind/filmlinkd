export default class EntryCommand {
    /**
     * @param {import('../services/letterboxd/api/letterboxd-log-entry-api')} letterboxdLogEntryApi
     * @param {import('../services/letterboxd/api/letterboxd-film-api')} letterboxdFilmApi
     * @param {import('../services/letterboxd/letterboxd-lid-web')} letterboxdLidWeb
     * @param {import('../factories/embed-builder-factory')} embedBuilderFactory
     */
    constructor(letterboxdFilmApi, letterboxdLidWeb, letterboxdLogEntryApi, embedBuilderFactory) {
        this.letterboxdFilmApi = letterboxdFilmApi;
        this.letterboxdLidWeb = letterboxdLidWeb;
        this.letterboxdLogEntryApi = letterboxdLogEntryApi;
        this.embedBuilderFactory = embedBuilderFactory;
    }

    /**
     * @param {string} accountName
     * @param {string} filmName
     * @returns {import('discord.js').EmbedBuilder}
     */
    getEmbed(accountName, filmName) {
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
                    throw 'Empty Logged Entry List Found';
                }

                return this.embedBuilderFactory.createLoggedEmbed(logEntryList);
            })
            .catch((e) => this.embedBuilderFactory.createNoLoggedEntriesFoundEmbed());
    }
}
