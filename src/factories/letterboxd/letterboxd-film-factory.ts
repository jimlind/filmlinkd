import LetterboxdFilmContributions from '../../models/letterboxd/letterboxd-film-contributions.js';
import LetterboxdFilmStatistics from '../../models/letterboxd/letterboxd-film-statistics.js';
import LetterboxdFilmSummary from '../../models/letterboxd/letterboxd-film-summary.js';
import LetterboxdFilm from '../../models/letterboxd/letterboxd-film.js';

export default class LetterboxdFilmFactory {
    /**
     * @param {import('./letterboxd-contributor-factory.mjs')} letterboxdContributorFactory
     * @param {import('./letterboxd-country-factory.mjs')} letterboxdCountryFactory
     * @param {import('./letterboxd-genre-factory.mjs')} letterboxdGenreFactory
     * @param {import('./letterboxd-image-factory.mjs')} letterboxdImageFactory
     * @param {import('./letterboxd-language-factory.mjs')} letterboxdLanguageFactory
     * @param {import('./letterboxd-link-factory.mjs')} letterboxdLinkFactory
     */
    constructor(
        readonly letterboxdContributorFactory: any,
        readonly letterboxdCountryFactory: any,
        readonly letterboxdGenreFactory: any,
        readonly letterboxdImageFactory: any,
        readonly letterboxdLanguageFactory: any,
        readonly letterboxdLinkFactory: any,
    ) {}

    /**
     * @param {Object} filmData
     * @returns LetterboxdFilm
     */
    buildFilmFromObject(filmData: any) {
        const letterboxdFilm = new LetterboxdFilm();

        letterboxdFilm.id = filmData.id || '';
        letterboxdFilm.name = filmData.name || '';
        letterboxdFilm.originalName = filmData.originalName || '';
        letterboxdFilm.alternativeNames = filmData.alternativeNames || [];
        letterboxdFilm.releaseYear = filmData.releaseYear || 0;
        letterboxdFilm.directors = (filmData.directors || []).map((director: any) =>
            this.letterboxdContributorFactory.buildContributorSummaryFromObject(director),
        );
        letterboxdFilm.poster = this.letterboxdImageFactory.buildImageFromObject(
            filmData.poster || {},
        );
        letterboxdFilm.adultPoster = this.letterboxdImageFactory.buildImageFromObject(
            filmData.adultPoster || {},
        );
        letterboxdFilm.top250Position = filmData.top250Position || null;
        letterboxdFilm.adult = filmData.adult || false;
        letterboxdFilm.filmCollectionId = filmData.filmCollectionId || '';
        letterboxdFilm.links = (filmData.links || []).map((link: any) =>
            this.letterboxdLinkFactory.buildLinkFromObject(link),
        );
        // TODO: Set this properly when/if needed
        letterboxdFilm.relationships = [];
        letterboxdFilm.tagline = filmData.tagline || '';
        letterboxdFilm.description = filmData.description || '';
        letterboxdFilm.runTime = filmData.runTime || 0;
        letterboxdFilm.backdrop = this.letterboxdImageFactory.buildImageFromObject(
            filmData.backdrop || {},
        );
        letterboxdFilm.backdropFocalPoint = filmData.backdropFocalPoint || 0;
        // TODO: Set this properly when/if needed
        letterboxdFilm.trailer = null;
        letterboxdFilm.genres = (filmData.genres || []).map((genre: any) =>
            this.letterboxdGenreFactory.buildGenreFromObject(genre),
        );
        letterboxdFilm.countries = (filmData.countries || []).map((country: any) =>
            this.letterboxdCountryFactory.buildCountryFromObject(country),
        );
        letterboxdFilm.languages = (filmData.languages || []).map((language: any) =>
            this.letterboxdLanguageFactory.buildLanguageFromObject(language),
        );
        letterboxdFilm.contributions = (filmData.contributions || []).map((contribution: any) =>
            this.buildFilmContributionsFromObject(contribution),
        );
        // TODO: Set this properly when/if needed
        letterboxdFilm.news = [];
        // TODO: Set this properly when/if needed
        letterboxdFilm.recentStories = [];

        return letterboxdFilm;
    }

    /**
     * @param {Object} filmSummaryData
     * @returns LetterboxdFilmSummary
     */
    buildFilmSummaryFromObject(filmSummaryData: any) {
        const letterboxdFilmSummary = new LetterboxdFilmSummary();

        letterboxdFilmSummary.id = filmSummaryData.id || '';
        letterboxdFilmSummary.name = filmSummaryData.name || '';
        letterboxdFilmSummary.alternativeNames = filmSummaryData.alternativeNames || [];
        letterboxdFilmSummary.releaseYear = filmSummaryData.releaseYear || 0;
        letterboxdFilmSummary.directors = (filmSummaryData.directors || []).map((director: any) =>
            this.letterboxdContributorFactory.buildContributorSummaryFromObject(director),
        );
        letterboxdFilmSummary.poster = this.letterboxdImageFactory.buildImageFromObject(
            filmSummaryData.poster || {},
        );
        letterboxdFilmSummary.adultPoster = this.letterboxdImageFactory.buildImageFromObject(
            filmSummaryData.adultPoster || {},
        );
        letterboxdFilmSummary.top250Position = filmSummaryData.top250Position || null;
        letterboxdFilmSummary.adult = filmSummaryData.adult || false;
        letterboxdFilmSummary.filmCollectionId = filmSummaryData.filmCollectionId || '';
        letterboxdFilmSummary.links = (filmSummaryData.links || []).map((link: any) =>
            this.letterboxdLinkFactory.buildLinkFromObject(link),
        );
        // TODO: Set this properly when/if needed
        letterboxdFilmSummary.relationships = [];

        return letterboxdFilmSummary;
    }

    buildFilmStatisticsFromObject(filmStatisticsData: any) {
        const letterboxdFilmStatistics = new LetterboxdFilmStatistics();

        letterboxdFilmStatistics.id = filmStatisticsData.film.id;
        letterboxdFilmStatistics.rating = filmStatisticsData?.rating || 0;
        letterboxdFilmStatistics.likeCount = filmStatisticsData?.counts?.likes || 0;
        letterboxdFilmStatistics.reviewCount = filmStatisticsData?.counts?.reviews || 0;
        letterboxdFilmStatistics.watchCount = filmStatisticsData?.counts?.watches || 0;

        return letterboxdFilmStatistics;
    }

    buildFilmContributionsFromObject(filmContributions: any) {
        const letterboxdFilmContributions = new LetterboxdFilmContributions();

        letterboxdFilmContributions.type = filmContributions.type;
        letterboxdFilmContributions.contributors = (filmContributions.contributors || []).map(
            (contributor: any) =>
                this.letterboxdContributorFactory.buildContributorSummaryFromObject(contributor),
        );

        return letterboxdFilmContributions;
    }
}
