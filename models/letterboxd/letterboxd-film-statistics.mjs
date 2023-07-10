/**
 * Letterboxd API FilmStatistics Object
 * https://api-docs.letterboxd.com/#/definitions/FilmStatistics
 */

export default class LetterboxdFilmStatistics {
    /** @property {string} id Letterboxd id */
    id = '';
    /** @property {numer} rating */
    rating = 0;
    /** @property {numer} likeCount */
    likeCount = 0;
    /** @property {numer} reviewCount */
    reviewCount = 0;
    /** @property {numer} watchCount */
    watchCount = 0;
}
