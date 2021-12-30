const LetterboxdGenre = require('../../models/letterboxd/letterboxd-genre');

class LetterboxdGenreFactory {
    /**
     * @param {Object} genreData
     * @returns LetterboxdGenre
     */
    buildGenreFromObject(genreData) {
        const letterboxdGenre = new LetterboxdGenre();

        letterboxdGenre.id = genreData.id;
        letterboxdGenre.name = genreData.name;

        return letterboxdGenre;
    }
}

module.exports = LetterboxdGenreFactory;
