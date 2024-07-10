/**
 * Letterboxd API Member Statistics Object
 * https://api-docs.letterboxd.com/#/definitions/MemberStatistics
 */

export default class LetterboxdMemberStatistics {
    /** @property {import('./letterboxd-member-identifier')} member The member for which statistics were requested. */
    member = null;
    /** @property {import('./letterboxd-member-statistics-counts')} counts The number of watches, ratings, likes, etc. for the member. */
    counts = null;
    // TODO: Define this properly
    /** @property {*[]} ratingsHistogram A summary of the number of ratings the member has made for each increment between 0.5 and 5.0. Returns only the integer increments between 1.0 and 5.0 if the member never (or rarely) awards half-star ratings. */
    ratingsHistogram = [];
    /** @property {number[]} yearsInReview A list of years the member has year-in-review pages for. Only supported for paying members. */
    yearsInReview = [];
}
