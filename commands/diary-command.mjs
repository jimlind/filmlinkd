'use strict';

class DiaryCommand {
    /**
     * @param {import('../services/letterboxd/letterboxd-lid-web')} letterboxdLidWeb
     * @param {import('../services/letterboxd/api/letterboxd-member-api')} letterboxdMemberApi
     * @param {import('../services/letterboxd/api/letterboxd-entry-api')} letterboxdEntryApi
     * @param {import('../factories/embed-builder-factory')} embedBuilderFactory
     */
    constructor(letterboxdLidWeb, letterboxdMemberApi, letterboxdEntryApi, embedBuilderFactory) {
        this.letterboxdLidWeb = letterboxdLidWeb;
        this.letterboxdMemberApi = letterboxdMemberApi;
        this.letterboxdEntryApi = letterboxdEntryApi;
        this.embedBuilderFactory = embedBuilderFactory;
    }

    /**
     * @param {string} accountName
     * @returns {Promise<import('discord.js').EmbedBuilder>}
     */
    getEmbed(accountName) {
        const lidPromise = this.letterboxdLidWeb.get(accountName);
        const promiseList = [
            lidPromise.then((lid) => this.letterboxdMemberApi.getMember(lid)),
            lidPromise.then((lid) => this.letterboxdEntryApi.get(lid, 5)),
        ];

        return Promise.all(promiseList)
            .then(([member, entryList]) =>
                this.embedBuilderFactory.createDiaryListEmbed(member, entryList),
            )
            .catch(() => this.embedBuilderFactory.createNoAccountFoundEmbed(accountName));
    }
}

module.exports = DiaryCommand;
