/**
 * Letterboxd API Contributor Object
 * https://api-docs.letterboxd.com/#/definitions/Contributor
 */

export default class LetterboxdContributor {
    /** @property {string} id The LID of the contributor. */
    id = '';
    /** @property {string} name The name of the contributor. */
    name = '';
    /** @property {string} tmdbid The ID of the contributor on TMDB, if known */
    tmdbid = '';
    /** @property {{contributions: import('./letterboxd-contribution-statistics')[]}} statistics An array of the types of contributions made, with a count of films for each contribution type. */
    statistics = null;
    /** @property {import('./letterboxd-link')[]} links A list of relevant URLs for this entity, on Letterboxd and external sites. */
    links = [];
}
