/**
 * Letterboxd API List Entry Summary Object
 * https://api-docs.letterboxd.com/#/definitions/ListEntrySummary
 */

class LetterboxdListEntrySummary {
    /** @property {number} rank If the list is ranked, this is the entry’s rank in the list, numbered from 1. */
    rank = 0;
    /** @property {import('./letterboxd-film-summary')} film The film for this entry. */
    film = null;
}

module.exports = LetterboxdListEntrySummary;
