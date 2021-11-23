'use strict';

class LetterboxdFilmApi {
    /**
     * @param {import('./letterboxd-api')} letterboxdApi
     * @param {import('../../../factories/letterboxd-film-factory')} letterboxdFilmFactory
     */
    constructor(letterboxdApi, letterboxdFilmFactory) {
        this.letterboxdApi = letterboxdApi;
        this.letterboxdFilmFactory = letterboxdFilmFactory;
    }

    /**
     * @param {string} filmName
     * @returns {Promise<string>}
     */
    search(filmName) {
        return this.letterboxdApi
            .get('search', {
                input: filmName,
                include: 'FilmSearchItem',
                perPage: 1,
                searchMethod: 'Autocomplete',
            })
            .then((responseData) => {
                const filmData = responseData?.items[0]?.film;
                if (!filmData) {
                    throw `Film not found for ${filmName}`;
                }
                return filmData.id;
            });
    }

    /**
     * @param {string} letterboxdId
     * @returns {Promise<import('../../../models/letterboxd/letterboxd-film')>}
     */
    getFilm(letterboxdId) {
        return this.letterboxdApi.get(`film/${letterboxdId}`, {}).then((responseData) => {
            return this.letterboxdFilmFactory.buildFilmFromObject(responseData);
        });
    }

    /**
     * @param {string} letterboxdId
     * @returns {Promise<import('../../../models/letterboxd/letterboxd-film-statistics')>}
     */
    getFilmStatistics(letterboxdId) {
        return this.letterboxdApi
            .get(`film/${letterboxdId}/statistics`, {})
            .then((responseData) => {
                return this.letterboxdFilmFactory.buildFilmStatisticsFromObject(responseData);
            });
    }
}

module.exports = LetterboxdFilmApi;
