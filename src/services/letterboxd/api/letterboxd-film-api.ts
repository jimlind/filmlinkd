export default class LetterboxdFilmApi {
    /**
     * @param {import('./letterboxd-api.mjs')} letterboxdApi
     * @param {import('../../../factories/letterboxd-film-factory')} letterboxdFilmFactory
     */
    constructor(readonly letterboxdApi: any, readonly letterboxdFilmFactory: any) {}

    /**
     * @param {string} filmName
     * @returns {Promise<string>}
     */
    search(filmName: any) {
        return this.letterboxdApi
            .get('search', {
                input: filmName,
                include: 'FilmSearchItem',
                perPage: 1,
                searchMethod: 'Autocomplete',
            })
            .then((responseData: any) => {
                const filmData = responseData?.items[0]?.film;
                if (!filmData) {
                    throw `Film not found for ${filmName}`;
                }
                return filmData.id;
            });
    }

    /**
     * @param {string} letterboxdId
     * @returns {Promise<import('../../../models/letterboxd/letterboxd-film.mjs')>}
     */
    getFilm(letterboxdId: any) {
        return this.letterboxdApi.get(`film/${letterboxdId}`, {}).then((responseData: any) => {
            return this.letterboxdFilmFactory.buildFilmFromObject(responseData);
        });
    }

    /**
     * @param {string} letterboxdId
     * @returns {Promise<import('../../../models/letterboxd/letterboxd-film-statistics.mjs')>}
     */
    getFilmStatistics(letterboxdId: any) {
        return this.letterboxdApi
            .get(`film/${letterboxdId}/statistics`, {})
            .then((responseData: any) => {
                return this.letterboxdFilmFactory.buildFilmStatisticsFromObject(responseData);
            });
    }
}
