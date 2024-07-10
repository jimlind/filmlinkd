'use strict';

export default class FilmCommand {
    /**
     * @param {import('../services/letterboxd/api/letterboxd-film-api.mjs')} letterboxdFilmApi
     * @param {import('../factories/embed-builder-factory.mjs')} embedBuilderFactory
     */
    constructor(readonly letterboxdFilmApi: any, readonly embedBuilderFactory: any) {}

    /**
     * @param {string} filmName
     * @returns {import('discord.js').EmbedBuilder}
     */
    getEmbed(filmName: any) {
        const searchFilm = this.letterboxdFilmApi.search(filmName);
        const promiseList = [
            searchFilm.then((lid: any) => this.letterboxdFilmApi.getFilm(lid)),
            searchFilm.then((lid: any) => this.letterboxdFilmApi.getFilmStatistics(lid)),
        ];

        return Promise.all(promiseList)
            .then(([film, filmStatistics]) =>
                this.embedBuilderFactory.createFilmEmbed(film, filmStatistics),
            )
            .catch(() => this.embedBuilderFactory.createFilmNotFoundEmbed());
    }
}
