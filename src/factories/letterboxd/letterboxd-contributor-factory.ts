import LetterboxdContributionStatistics from '../../models/letterboxd/letterboxd-contribution-statistics.js';
import LetterboxdContributorSummary from '../../models/letterboxd/letterboxd-contributor-summary.js';
import LetterboxdContributor from '../../models/letterboxd/letterboxd-contributor.js';

export default class LetterboxdContributorFactory {
    /**
     * @param {import('./letterboxd-link-factory.mjs')} letterboxdLinkFactory
     */
    constructor(readonly letterboxdLinkFactory: any) {}

    /**
     * @param {Object} contributorData
     * @returns LetterboxdContributor
     */
    buildContributorFromObject(contributorData: any) {
        const letterboxdContributor = new LetterboxdContributor();

        letterboxdContributor.id = contributorData.id;
        letterboxdContributor.name = contributorData.name;
        letterboxdContributor.tmdbid = contributorData.tmdbid;
        letterboxdContributor.statistics = {
            contributions: (contributorData?.statistics?.contributions || []).map(
                (statisticsData: any) => this.buildContributionStatisticsFromObject(statisticsData),
            ),
        } as any;
        letterboxdContributor.links = (contributorData.links || []).map((linkData: any) =>
            this.letterboxdLinkFactory.buildLinkFromObject(linkData),
        );

        return letterboxdContributor;
    }

    buildContributorSummaryFromObject(contributorSummaryData: any) {
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
    buildContributionStatisticsFromObject(contributionStatisticsData: any) {
        const letterboxdContributionStatistics = new LetterboxdContributionStatistics();

        letterboxdContributionStatistics.type = contributionStatisticsData.type;
        letterboxdContributionStatistics.filmCount = contributionStatisticsData.filmCount;

        return letterboxdContributionStatistics;
    }
}
