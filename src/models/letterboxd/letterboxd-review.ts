/**
 * Letterboxd API Review Object
 * https://api-docs.letterboxd.com/#/definitions/Review
 */

export default class LetterboxdReview {
    /** @property {string} id The LID of the list. */
    id = '';
    /** @property {string} lbml The review text in LBML. May contain the following HTML tags: <br> <strong> <em> <b> <i> <a href=""> <blockquote>. */
    lbml = '';
    /** @property {string} containsSpoilers Will be true if the member has indicated that the review field contains plot spoilers for the film. */
    containsSpoilers = false;
    /** @property {string} spoilersLocked Will be true if the spoilers flag has been locked by a moderator. */
    spoilersLocked = false;
    /** @property {string} moderated Will be true if the review has been removed by a moderator. */
    moderated = false;
    // TODO: canShareOn is an enum
    /** @property {string} canShareOn The third-party service or services to which this review can be shared. Only included if the authenticated member is the review’s owner. DEPRECATED No longer supported by Facebook. */
    canShareOn = '';
    // TODO: sharedOn is an enum
    /** @property {string} sharedOn The third-party service or services to which this review has been shared. Only included if the authenticated member is the review’s owner. DEPRECATED No longer supported by Facebook. */
    sharedOn = '';
    /** @property {string} whenReviewed The timestamp when this log entry’s review was first published, in ISO 8601 format with UTC timezone, i.e. YYYY-MM-DDThh:mm:ssZ */
    whenReviewed = '';
    /** @property {string} text The review text formatted as HTML. */
    text = '';
}
