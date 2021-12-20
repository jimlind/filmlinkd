/**
 * Letterboxd API Link Object
 * https://api-docs.letterboxd.com/#/definitions/Pronoun
 */

class LetterboxdPronoun {
    /** @property {string} id The LID for this pronoun. */
    id = '';
    /** @property {string} label A label to describe this pronoun. */
    label = '';
    /** @property {string} subjectPronoun The pronoun to use when the member is the subject. */
    subjectPronoun = '';
    /** @property {string} objectPronoun The pronoun to use when the member is the object. */
    objectPronoun = '';
    /** @property {string} possessiveAdjective The adjective to use when describing a specified thing or things belonging to or associated with a member previously mentioned. */
    possessiveAdjective = '';
    /** @property {string} possessivePronoun The pronoun to use when referring to a specified thing or things belonging to or associated with a member previously mentioned. */
    possessivePronoun = '';
    /** @property {string} reflexive The pronoun to use to refer back to the member. */
    reflexive = '';
}

module.exports = LetterboxdPronoun;
