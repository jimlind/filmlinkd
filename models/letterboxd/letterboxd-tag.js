/**
 * Letterboxd API Tag Object
 * https://api-docs.letterboxd.com/#/definitions/Tag
 */

class LetterboxdTag {
    /** @property {string} tag DEPRECATED Use displayTag instead. */
    tag = '';
    /** @property {string} code The tag code. */
    code = '';
    /** @property {string} displayTag The tag text as entered by the tagger. */
    displayTag = '';
}

module.exports = LetterboxdTag;
