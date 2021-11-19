class LetterboxdEntry {
    /** @property {string} id Letterboxd id */
    id = '';
    /** @property {string} url Letterboxd entry link */
    url = '';
    /** @property {import('./letterboxd-member')} member Letterboxd member object */
    member = null;
    /** @property {string} filmName film name */
    filmName = '';
    /** @property {number} filmYear release year */
    filmYear = 0;
    /** @property {Date} date watch date */
    date = null;
    /** @property {string} review full html review text */
    review = '';
    /** @property {boolean} rewatch rewatch status*/
    rewatch = false;
    /** @property {number} rating star rating */
    rating = 0;
    /** @property {boolean} like liked status  */
    like = false;
}

module.exports = LetterboxdEntry;
