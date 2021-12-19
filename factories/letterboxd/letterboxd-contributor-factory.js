const LetterboxdContributionStatistics = require('../../models/letterboxd/letterboxd-contribution-statistics');
const LetterboxdContributor = require('../../models/letterboxd/letterboxd-contributor');

class LetterboxdContributorFactory {
    /**
     * @param {import('./letterboxd-link-factory')} letterboxdLinkFactory
     */
    constructor(letterboxdLinkFactory) {
        this.letterboxdLinkFactory = letterboxdLinkFactory;
    }

    /**
     * @param {Object} contributorData
     * @returns LetterboxdContributor
     */
    buildContributorFromObject(contributorData) {
        const letterboxdContributor = new LetterboxdContributor();

        letterboxdContributor.id = contributorData.id;
        letterboxdContributor.name = contributorData.name;
        letterboxdContributor.tmdbid = contributorData.tmdbid;
        letterboxdContributor.statistics = {
            contributions: contributorData.statistics.contributions.map(
                this.buildContributionStatisticsFromObject,
            ),
        };
        letterboxdContributor.links = contributorData.links.map(
            this.letterboxdLinkFactory.buildLinkFromObject,
        );

        return letterboxdContributor;
    }

    /**
     * @param {Object} contributionStatisticsData
     * @returns LetterboxdContributionStatistics
     */
    buildContributionStatisticsFromObject(contributionStatisticsData) {
        const letterboxdContributionStatistics = new LetterboxdContributionStatistics();

        letterboxdContributionStatistics.type = contributionStatisticsData.type;
        letterboxdContributionStatistics.filmCount = contributionStatisticsData.filmCount;

        return letterboxdContributionStatistics;
    }
}

module.exports = LetterboxdContributorFactory;
