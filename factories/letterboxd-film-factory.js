const LetterboxdFilm = require('../models/letterboxd/letterboxd-film');
const LetterboxdFilmStatistics = require('../models/letterboxd/letterboxd-film-statistics');

class LetterboxdFilmFactory {
    /**
     * @param {Object} entryData
     * @returns LetterboxdEntry
     */
    buildFilmFromObject(filmData) {
        const letterboxdFilm = new LetterboxdFilm();

        letterboxdFilm.id = filmData.id;
        letterboxdFilm.name = filmData.name;
        letterboxdFilm.year = filmData.releaseYear;
        letterboxdFilm.tagline = filmData.tagline;
        letterboxdFilm.runTime = filmData.runTime;
        letterboxdFilm.genreList = filmData.genres.map((g) => g.name);
        letterboxdFilm.countryList = filmData.countries.map((c) => c.name);

        // Parse list of directors
        const filteredDirector = filmData.contributions.filter((c) => c.type == 'Director');
        letterboxdFilm.directorList = filteredDirector[0]?.contributors?.map((d) => d.name) || [];

        // Parse largest image url
        const largestImage = filmData.poster.sizes.reduce((previous, current) =>
            current.height > previous.height ? current : previous,
        );
        letterboxdFilm.image = largestImage.url;

        return letterboxdFilm;
    }

    buildFilmStatisticsFromObject(filmStatisticsData) {
        const letterboxdFilmStatistics = new LetterboxdFilmStatistics();

        letterboxdFilmStatistics.id = filmStatisticsData.film.id;
        letterboxdFilmStatistics.rating = filmStatisticsData?.rating || 0;
        letterboxdFilmStatistics.likeCount = filmStatisticsData?.counts?.likes || 0;
        letterboxdFilmStatistics.reviewCount = filmStatisticsData?.counts?.reviews || 0;
        letterboxdFilmStatistics.watchCount = filmStatisticsData?.counts?.watches || 0;

        return letterboxdFilmStatistics;
    }
}

module.exports = LetterboxdFilmFactory;
