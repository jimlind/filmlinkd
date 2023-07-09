'use strict';

export default class FilmCommand {
    /**
     * @param {import('../services/letterboxd/api/letterboxd-film-api')} letterboxdFilmApi
     * @param {import('../factories/embed-builder-factory.mjs')} embedBuilderFactory
     */
    constructor(letterboxdFilmApi, embedBuilderFactory) {
        this.letterboxdFilmApi = letterboxdFilmApi;
        this.embedBuilderFactory = embedBuilderFactory;
    }

    /**
     * @param {string} filmName
     * @returns {import('discord.js').EmbedBuilder}
     */
    getEmbed(filmName) {
        const searchFilm = this.letterboxdFilmApi.search(filmName);
        const promiseList = [
            searchFilm.then((lid) => this.letterboxdFilmApi.getFilm(lid)),
            searchFilm.then((lid) => this.letterboxdFilmApi.getFilmStatistics(lid)),
        ];

        return Promise.all(promiseList)
            .then(([film, filmStatistics]) =>
                this.embedBuilderFactory.createFilmEmbed(film, filmStatistics),
            )
            .catch(() => this.embedBuilderFactory.createFilmNotFoundEmbed());
    }
}
