const LetterboxdContributionStatistics = require('../../models/letterboxd/letterboxd-contribution-statistics');

class LetterboxdContributionStatisticsFactory {
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

module.exports = LetterboxdContributionStatisticsFactory;
