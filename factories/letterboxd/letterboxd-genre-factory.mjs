import LetterboxdGenre from '../../models/letterboxd/letterboxd-genre.mjs';

export default class LetterboxdGenreFactory {
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
