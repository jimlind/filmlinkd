const LetterboxdContributor = require('../../models/letterboxd/letterboxd-contributor');

class LetterboxdContributorFactory {
    /**
     * @param {import('./letterboxd-contribution-statistic-factory')} letterboxdContributionStatisticsFactory
     * @param {import('./letterboxd-link-factory')} letterboxdLinkFactory
     */
    constructor(letterboxdContributionStatisticsFactory, letterboxdLinkFactory) {
        this.letterboxdContributionStatisticsFactory = letterboxdContributionStatisticsFactory;
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
                this.letterboxdContributionStatisticsFactory.buildContributionStatisticsFromObject,
            ),
        };
        letterboxdContributor.links = contributorData.links.map(
            this.letterboxdLinkFactory.buildLinkFromObject,
        );

        return letterboxdContributor;
    }
}

module.exports = LetterboxdContributorFactory;
