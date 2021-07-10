class DiaryEntry {
    /** @property {number} id - Letterboxd id for diary entry */
    id = 0;
    /** @property {string} type - Mushy string for watched or reviewed status */
    type = '';
    /** @property {string} link - Letterboxd link to diary entry */
    link = '';
    /** @property {number} publishedDate - Unix timestamp of review published */
    publishedDate = 0;
    /** @property {string} filmTitle - The title of the film */
    filmTitle = '';
    /** @property {number} filmYear - The year the film was released */
    filmYear = 0;
    /** @property {number} watchedDate - Unix timestamp of film watched */
    watchedDate = 0;
    /** @property {string} image - URL for the film poster image */
    image = '';
    /** @property {number} starCoutn - Numerical representation of stars */
    starCount = 0;
    /** @property {string} stars - Unicode string representation of stars */
    stars = '';
    /** @property {boolean} rewatch - Was this entered as a rewatch */
    rewatch = false;
    /** @property {boolean} liked - Was this entered as a like/favorited/hearted */
    liked = false;
    /** @property {boolean} containsSpoilers - Does the review contain spoilers */
    containsSpoilers = false;
    /** @property {string} review - Full text of the review */
    review = '';
}

module.exports = DiaryEntry;
