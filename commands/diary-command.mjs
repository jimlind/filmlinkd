'use strict';

export default class DiaryCommand {
    /**
     * @param {import('../services/letterboxd/letterboxd-lid-web.mjs')} letterboxdLidWeb
     * @param {import('../services/letterboxd/api/letterboxd-member-api.mjs')} letterboxdMemberApi
     * @param {import('../services/letterboxd/api/letterboxd-entry-api.mjs')} letterboxdEntryApi
     * @param {import('../factories/embed-builder-factory.mjs')} embedBuilderFactory
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
