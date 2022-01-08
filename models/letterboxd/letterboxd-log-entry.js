/**
 * Letterboxd API Log Entry Object
 * https://api-docs.letterboxd.com/#/definitions/LogEntry
 */

class LetterboxdLogEntry {
    /** @property {string} id The LID of the log entry. */
    id = '';
    /** @property {string} name A descriptive title for the log entry.  */
    name = '';
    /** @property {import('./letterboxd-member-summary')} owner The member who created the log entry. */
    owner = null;
    /** @property {import('./letterboxd-film-summary')} film The film being logged. Includes a MemberFilmRelationship for the member who created the log entry. */
    film = null;
    /** @property {import('./letterboxd-diary-details)} diaryDetails Details about the log entry, if present */
    diaryDetails = null;
    /** @property {import('./letterboxd-review')} review Review details for the log entry, if present. */
    review = null;
    /** @property {string[]} tags DEPRECATED Use tags2 instead. */
    tags = [];
    /** @property {import('./letterboxd-tag')[]} tags2 The tags for the log entry. */
    tags2 = [];
    /** @property {string} whenCreated The timestamp of when the log entry was created, in ISO 8601 format with UTC timezone, i.e. YYYY-MM-DDThh:mm:ssZ */
    whenCreated = '';
    /** @property {string} whenUpdated The timestamp of when the log entry was last updated, in ISO 8601 format with UTC timezone, i.e. YYYY-MM-DDThh:mm:ssZ */
    whenUpdated = '';
    /** @property {number} rating The member’s rating for the film. Allowable values are between 0.5 and 5.0, with increments of 0.5. */
    rating = 0;
    /** @property {boolean} like Will be true if the member likes the film (via the ‘heart’ icon). */
    like = false;
    /** @property {boolean} commentable Will be true if comments can be posted to the log entry by the member. This is determined according to the existence of review text and other factors such as the content owner’s comment policy. */
    commentable = false;
    // TODO: commentPolicy is an enum
    /** @property {string} commentPolicy The policy determining who can post comments to the log entry. You in this context refers to the content owner. Use the commentThreadState property of the ListRelationship to determine the signed-in member’s ability to comment (or not). */
    commentPolicy = '';
    /** @property {import('./letterboxd-link')[]} links A list of relevant URLs for this entity, on Letterboxd and external sites. */
    links = [];
    /** @property {import('./letterboxd-image')} backdrop The log entry's backdrop image at multiple sizes, sourced from the film being logged, if available. Only returned for Patron members. */
    backdrop = null;
    /** @property {number} backdropFocalPoint The vertical focal point of the log entry’s backdrop image, if available. Expressed as a proportion of the image’s height, using values between 0.0 and 1.0. Use when cropping the image into a shorter space, such as in the page for a film on the Letterboxd site. */
    backdropFocalPoint = 0;
}

module.exports = LetterboxdLogEntry;
