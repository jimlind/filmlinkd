'use strict';

class ContributorCommand {
    /**
     * @param {import('../services/letterboxd/api/letterboxd-contributor-api')} letterboxdContributorApi
     * @param {import('../factories/message-embed-factory')} messageEmbedFactory
     */
    constructor(letterboxdContributorApi, messageEmbedFactory) {
        this.letterboxdContributorApi = letterboxdContributorApi;
        this.messageEmbedFactory = messageEmbedFactory;
    }

    /**
     * @param {string} contributorName
     * @returns {Promise<import('discord.js').MessageEmbed>}
     */
    getMessage(contributorName) {
        // TODO:
        // Also get the image of the contributor from thier Letterboxd page.
        // It isn't accessible on the API.
        return this.letterboxdContributorApi
            .getContributor(contributorName)
            .then((contributor) => {
                return this.messageEmbedFactory.createContributorMessage(contributor);
            })
            .catch(() => this.messageEmbedFactory.createContributorNotFoundMessage());
    }
}

module.exports = ContributorCommand;
