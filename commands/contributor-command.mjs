'use strict';

export default class ContributorCommand {
    /**
     * @param {import('../services/letterboxd/api/letterboxd-contributor-api.mjs')} letterboxdContributorApi
     * @param {import('../factories/embed-builder-factory.mjs')} embedBuilderFactory
     */
    constructor(letterboxdContributorApi, embedBuilderFactory) {
        this.letterboxdContributorApi = letterboxdContributorApi;
        this.embedBuilderFactory = embedBuilderFactory;
    }

    /**
     * @param {string} contributorName
     * @returns {Promise<import('discord.js').EmbedBuilder>}
     */
    getEmbed(contributorName) {
        // TODO:
        // Also get the image of the contributor from thier Letterboxd page.
        // It isn't accessible on the API.
        return this.letterboxdContributorApi
            .getContributor(contributorName)
            .then((contributor) => {
                return this.embedBuilderFactory.createContributorEmbed(contributor);
            })
            .catch(() => this.embedBuilderFactory.createContributorNotFoundEmbed());
    }
}
