/**
 * Letterboxd API Contribution Statistics Object
 * https://api-docs.letterboxd.com/#/definitions/ContributionStatistics
 */

class LetterboxdContributionStatistics {
    /** @property {string} type The type of contribution. [TECHNICALLY AN ENUM] */
    type = '';
    /** @property {integer} filmCount The number of films for this contribution type. */
    filmCount = 0;
}

module.exports = LetterboxdContributionStatistics;
