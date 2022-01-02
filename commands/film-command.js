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
        const promiseList = [
            searchFilm.then((lid) => this.letterboxdFilmApi.getFilm(lid)),
            searchFilm.then((lid) => this.letterboxdFilmApi.getFilmStatistics(lid)),
        ];

        return Promise.all(promiseList)
            .then(([film, filmStatistics]) =>
                this.messageEmbedFactory.createFilmMessage(film, filmStatistics),
            )
            .catch(() => this.messageEmbedFactory.createFilmNotFoundMessage());
    }
}

module.exports = FilmCommand;
