class LetterboxdFilm {
    /** @property {string} id Letterboxd id */
    id = '';
    /** @property {string} name film name */
    name = '';
    /** @property {string} year film release year */
    year = 0;
    /** @property {string} image URL for movie poster */
    image = '';
    /** @property {string} tagline film tagline */
    tagline = '';
    /** @property {string[]} director film director */
    directors = [];
    /** @property {string[]} countries film production countries */
    countries = [];
    /** @property {number} tagline film run time in mimnutes */
    runTime = 0;
    /** @property {string[]} countries film genres */
    genres = [];
}

module.exports = LetterboxdFilm;
