'use strict';

class FilmCommand {
    /**
     * @param {import('../services/letterboxd/api/letterboxd-film-api')} letterboxdFilmApi
     * @param {import('../factories/message-embed-factory')} messageEmbedFactory
     */
    constructor(letterboxdFilmApi, messageEmbedFactory) {
        this.letterboxdFilmApi = letterboxdFilmApi;
        this.messageEmbedFactory = messageEmbedFactory;
    }

    /**
     * @param {string} filmName
     * @returns {import('discord.js').MessageEmbed}
     */
    getMessage(filmName) {
        const searchFilm = this.letterboxdFilmApi.search(filmName);
        const getFilm = searchFilm.then((letterboxdId) => {
            return this.letterboxdFilmApi.getFilm(letterboxdId);
        });
        const getFilmStatistics = searchFilm.then((letterboxdId) => {
            return this.letterboxdFilmApi.getFilmStatistics(letterboxdId);
        });

        return Promise.all([getFilm, getFilmStatistics])
            .then(([film, filmStatistics]) =>
                this.messageEmbedFactory.createFilmMessage(film, filmStatistics),
            )
            .catch(() => {
                return this.messageEmbedFactory.createFilmNotFoundMessage();
            });
    }
}

module.exports = FilmCommand;
