'use strict';

class DiaryCommand {
    /**
     * @param {import('../services/letterboxd/letterboxd-lid-web')} letterboxdLidWeb
     * @param {import('../services/letterboxd/api/letterboxd-member-api')} letterboxdMemberApi
     * @param {import('../services/letterboxd/api/letterboxd-entry-api')} letterboxdEntryApi
     * @param {import('../factories/message-embed-factory')} messageEmbedFactory
     */
    constructor(letterboxdLidWeb, letterboxdMemberApi, letterboxdEntryApi, messageEmbedFactory) {
        this.letterboxdLidWeb = letterboxdLidWeb;
        this.letterboxdMemberApi = letterboxdMemberApi;
        this.letterboxdEntryApi = letterboxdEntryApi;
        this.messageEmbedFactory = messageEmbedFactory;
    }

    /**
     * @param {string} accountName
     * @returns {Promise<import('discord.js').MessageEmbed>}
     */
    getMessage(accountName) {
        const lidPromise = this.letterboxdLidWeb.get(accountName);
        const promiseList = [
            lidPromise.then((lid) => this.letterboxdMemberApi.getMember(lid)),
            lidPromise.then((lid) => this.letterboxdEntryApi.get(lid, 5)),
        ];

        return Promise.all(promiseList)
            .then(([member, entryList]) =>
                this.messageEmbedFactory.createDiaryListMessage(member, entryList),
            )
            .catch(() => this.messageEmbedFactory.createNoAccountFoundMessage(accountName));
    }
}

module.exports = DiaryCommand;
