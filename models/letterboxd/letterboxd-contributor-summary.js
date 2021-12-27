/**
 * Letterboxd API ContributorSummary Object
 * https://api-docs.letterboxd.com/#/definitions/ContributorSummary
 */

class LetterboxdContributorSummary {
    /** @property {string} id The LID of the contributor. */
    id = '';
    /** @property {string} name The name of the contributor. */
    name = '';
    /** @property {string} characterName The character name if available (only if the contribution is as an Actor; see the type field in FilmContributions). */
    characterName = '';
    /** @property {string} tmdbid The ID of the contributor on TMDB, if known */
    tmdbid = '';
}

module.exports = LetterboxdContributorSummary;
