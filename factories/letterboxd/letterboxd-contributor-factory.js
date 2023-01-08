const LetterboxdContributionStatistics = require('../../models/letterboxd/letterboxd-contribution-statistics');
const LetterboxdContributor = require('../../models/letterboxd/letterboxd-contributor');
const LetterboxdContributorSummary = require('../../models/letterboxd/letterboxd-contributor-summary');

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
            contributions: (contributorData?.statistics?.contributions || []).map(
                (statisticsData) => this.buildContributionStatisticsFromObject(statisticsData),
            ),
        };
        letterboxdContributor.links = (contributorData.links || []).map((linkData) =>
            this.letterboxdLinkFactory.buildLinkFromObject(linkData),
        );

        return letterboxdContributor;
    }

    buildContributorSummaryFromObject(contributorSummaryData) {
        const letterboxdContributorSummary = new LetterboxdContributorSummary();

        letterboxdContributorSummary.id = contributorSummaryData.id;
        letterboxdContributorSummary.name = contributorSummaryData.name;
        letterboxdContributorSummary.characterName = contributorSummaryData.characterName;
        letterboxdContributorSummary.tmdbid = contributorSummaryData.tmdbid;

        return letterboxdContributorSummary;
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
