'use strict';

class DiaryCommand {
    /**
     * @param {import('../services/letterboxd/api/letterboxd-member-api')} letterboxdMemberApi
     * @param {import('../services/letterboxd/api/letterboxd-entry-api')} letterboxdEntryApi
     * @param {import('../factories/message-embed-factory')} messageEmbedFactory
     */
    constructor(letterboxdMemberApi, letterboxdEntryApi, messageEmbedFactory) {
        this.letterboxdMemberApi = letterboxdMemberApi;
        this.letterboxdEntryApi = letterboxdEntryApi;
        this.messageEmbedFactory = messageEmbedFactory;
    }

    /**
     * @param {string} accountName
     * @returns {import('discord.js').MessageEmbed}
     */
    getMessage(accountName) {
        const getMember = this.letterboxdMemberApi.get(accountName);
        const getEntryList = getMember.then((member) => {
            return this.letterboxdEntryApi.get(member.id, 5);
        });

        return Promise.all([getMember, getEntryList])
            .then(([member, entryList]) =>
                this.messageEmbedFactory.createDiaryListMessage(member, entryList),
            )
            .catch(() => this.messageEmbedFactory.createNoAccountFoundMessage(accountName));
    }
}

module.exports = DiaryCommand;
