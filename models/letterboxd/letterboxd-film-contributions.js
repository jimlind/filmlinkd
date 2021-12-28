/**
 * Letterboxd API Film Contributions Object
 * https://api-docs.letterboxd.com/#/definitions/FilmContributions
 */

class LetterboxdFilmContributions {
    // TODO: type is an enum
    /** @property {string} type The type of contribution. */
    type = '';
    /** @property {import('./letterboxd-contributor-summary')} contributors The list of contributors of the specified type for the film. */
    contributors = [];
}

module.exports = LetterboxdFilmContributions;
