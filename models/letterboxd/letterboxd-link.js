/**
 * Letterboxd API Contributor Object
 * https://api-docs.letterboxd.com/#/definitions/Link
 */

class LetterboxdLink {
    /** @property {string} type Denotes which site the link is for. [TECHNICALLY AN ENUM] */
    type = '';
    /** @property {string} id The object ID for the linked entity on the destination site. */
    id = '';
    /** @property {string} url The fully qualified URL on the destination site. */
    url = '';
}

module.exports = LetterboxdLink;
