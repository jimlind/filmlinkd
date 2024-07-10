export default class LetterboxdContributorApi {
    /**
     * @param {import('./letterboxd-api.mjs')} letterboxdApi
     * @param {import('../../../factories/letterboxd/letterboxd-contributor-factory.mjs')} letterboxdContributorFactory
     */
    constructor(readonly letterboxdApi: any, readonly letterboxdContributorFactory: any) {
        this.letterboxdApi = letterboxdApi;
        this.letterboxdContributorFactory = letterboxdContributorFactory;
    }

    /**
     * @param {string} contributorName
     * @returns {Promise<import('../../../models/letterboxd/letterboxd-contributor.mjs')>}
     */
    getContributor(contributorName: string) {
        return this.letterboxdApi
            .get('search', {
                input: contributorName,
                include: 'ContributorSearchItem',
                perPage: 1,
            })
            .then((responseData: any) => {
                const contributorData = responseData?.items[0]?.contributor;
                if (!contributorData) {
                    throw `Contributor not found for ${contributorName}`;
                }
                return this.letterboxdContributorFactory.buildContributorFromObject(
                    contributorData,
                );
            });
    }
}
