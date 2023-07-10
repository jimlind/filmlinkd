/**
 * Letterboxd API Tag Object
 * https://api-docs.letterboxd.com/#/definitions/Tag
 */

export default class LetterboxdTag {
    /** @property {string} tag DEPRECATED Use displayTag instead. */
    tag = '';
    /** @property {string} code The tag code. */
    code = '';
    /** @property {string} displayTag The tag text as entered by the tagger. */
    displayTag = '';
}
