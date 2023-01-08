/**
 * Letterboxd API List Summary Object
 * https://api-docs.letterboxd.com/#/definitions/ListSummary
 */

class LetterboxdListSummary {
    /** @property {string} id The LID of the list. */
    id = '';
    /** @property {string} name The name of the list. */
    name = '';
    /** @property {number} filmCount The number of films in the list. */
    filmCount = 0;
    /** @property {boolean} published Will be true if the owner has elected to publish the list for other members to see. */
    published = true;
    /** @property {boolean} ranked Will be true if the owner has elected to make this a ranked list. */
    ranked = false;
    /** @property {string} descriptionLbml The list description in LBML. May contain the following HTML tags: <br> <strong> <em> <b> <i> <a href=""> <blockquote>. The text is a preview extract, and may be truncated if it’s too long. */
    descriptionLbml = '';
    /** @property {boolean} descriptionTruncated Will be true if the list description was truncated because it’s very long. */
    descriptionTruncated = false;
    /** @property {import('./letterboxd-member-summary')} owner The member who owns the list. */
    owner = null;
    /** @property {import('./letterboxd-list-identifier')} clonedFrom The list this was cloned from, if applicable. */
    clonedFrom = null;
    /** @property {import('./letterboxd-list-entry-summary')[]} previewEntries The first 12 entries in the list. To fetch more than 12 entries, and to fetch the entry notes, use the /list/{id}/entries endpoint. */
    previewEntries = [];
    // TODO: Define this properly
    /** @property {*[]} entriesOfNote Returned when one or more filmsOfNote is specified in the request. Contains, for each list in the response, the rank position of each film of note (if in the list) or -1 (if not). */
    entriesOfNote = [];
    /** @property {string} description The list description formatted as HTML. The text is a preview extract, and may be truncated if it’s too long. */
    description = '';
}

module.exports = LetterboxdListSummary;
