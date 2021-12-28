/**
 * Letterboxd API Film Object
 * https://api-docs.letterboxd.com/#/definitions/Film
 */

class LetterboxdFilm {
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
    /** @property {import('./letterboxd-image')} poster The film’s poster image (2:3 aspect ratio in multiple sizes). Will contain only a single obfuscated image if the adult flag is true. */
    poster = null;
    /** @property {import('./letterboxd-image')} adultPoster The film’s unobfuscated poster image (2:3 aspect ratio in multiple sizes), only populated if the adult flag is true, may contain adult content. */
    adultPoster = null;
    /** @property {number|null} top250Position The film’s position in the official Letterboxd Top 250 list of narrative feature films, null if the film is not in the list. */
    top250Position = null;
    /** @property {boolean} adult true if the film is in TMDb’s ‘Adult’ category.*/
    adult = false;
    /** @property {string} filmCollectionId The LID of the collection containing this film. */
    filmCollectionId = '';
    /** @property {import('./letterboxd-link')[]} links A list of relevant URLs for this entity, on Letterboxd and external sites. */
    links = [];
    // TODO: Define this properly
    /** @property {*[]} relationships Relationships to the film for the authenticated member (if any) and other members where relevant. */
    relationships = [];
    /** @property {string} tagline The tagline for the film. */
    tagline = '';
    /** @property {string} description A synopsis of the film. */
    description = '';
    /** @property {number} tagline The film’s duration (in minutes). */
    runTime = 0;
    /** @property {import('./letterboxd-image')} backdrop The film’s backdrop image (16:9 ratio in multiple sizes). */
    backdrop = null;
    /** @property {number} backdropFocalPoint The backdrop’s vertical focal point, expressed as a proportion of the image’s height, using values between 0.0 and 1.0. Use when cropping the image into a shorter space, such as in the page for a film on the Letterboxd site.  */
    backdropFocalPoint = 0;
    // TODO: Define this properly
    /** @property {*} trailer The film’s trailer. */
    trailer = null;
    // TODO: Define this properly
    /** @property {*[]} genres The film’s genres. */
    genres = [];
    // TODO: Define this properly
    /** @property {*[]} countries The film’s production countries. */
    countries = [];
    // TODO: Define this properly
    /** @property {*[]} languages The film’s spoken languages. */
    languages = [];
    // TODO: Define this properly
    /** @property {*[]} contributions The film’s contributors (director, cast and crew) grouped by discipline. */
    contributions = [];
    // TODO: Define this properly
    /** @property {*[]} news The related news items for the film. */
    news = [];
    // TODO: Define this properly
    /** @property {*[]} recentStories The related recent stories for a film. */
    recentStories = [];
}

module.exports = LetterboxdFilm;
