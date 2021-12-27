/**
 * Letterboxd API FilmSummary Object
 * https://api-docs.letterboxd.com/#/definitions/FilmSummary
 */

class LetterboxdFilmSummary {
    /** @property {string} id The LID of the film. */
    id = '';
    /** @property {string} name The title of the film. */
    name = '';
    /** @property {string} originalName The original title of the film, if it was first released with a non-English title. */
    originalName = '';
    /** @property {string[]} alternativeNames The other names by which the film is known (including alternative titles and/or foreign translations). */
    alternativeNames = [];
    /** @property {number} releaseYear The year in which the film was first released. */
    releaseYear = 0;
    /** @property {import('./letterboxd-contributor-summary')[]} directors The list of directors for the film. */
    directors = [];
    /** @property {import('./letterboxd-image') | null} poster The film’s poster image (2:3 aspect ratio in multiple sizes). Will contain only a single obfuscated image if the adult flag is true. */
    poster = null;
    /** @property {import('./letterboxd-image') | null} adultPoster The film’s unobfuscated poster image (2:3 aspect ratio in multiple sizes), only populated if the adult flag is true, may contain adult content. */
    adultPoster = null;
    /** @property {number | null} top250Position The film’s position in the official Letterboxd Top 250 list of narrative feature films, null if the film is not in the list. */
    top250Position = null;
    /** @property {boolean} adult true if the film is in TMDb’s ‘Adult’ category. */
    adult = false;
    /** @property {string} filmCollectionId The LID of the collection containing this film. */
    filmCollectionId = '';
    /** @property {import('./letterboxd-link')[]} links A list of relevant URLs for this entity, on Letterboxd and external sites. */
    links = [];
    // TODO: Define this properly
    /** @property {*} relationships Relationships to the film for the authenticated member (if any) and other members where relevant. */
    relationships = [];
}

module.exports = LetterboxdFilmSummary;
