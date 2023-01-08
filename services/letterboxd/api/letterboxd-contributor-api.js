'use strict';

class LetterboxdContributorApi {
    /**
     * @param {import('./letterboxd-api')} letterboxdApi
     * @param {import('../../../factories/letterboxd/letterboxd-contributor-factory')} letterboxdContributorFactory
     */
    constructor(letterboxdApi, letterboxdContributorFactory) {
        this.letterboxdApi = letterboxdApi;
        this.letterboxdContributorFactory = letterboxdContributorFactory;
    }

    /**
     * @param {string} contributorName
     * @returns {Promise<import('../../../models/letterboxd/letterboxd-contributor')>}
     */
    getContributor(contributorName) {
        return this.letterboxdApi
            .get('search', {
                input: contributorName,
                include: 'ContributorSearchItem',
                perPage: 1,
            })
            .then((responseData) => {
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

module.exports = LetterboxdContributorApi;
