class UserCommand {
    /**
     * @param {import('../services/letterboxd/letterboxd-lid-web')} letterboxdLidWeb
     * @param {import('../services/letterboxd/api/letterboxd-member-api')} letterboxdMemberApi
     * @param {import('../factories/message-embed-factory')} messageEmbedFactory
     */
    constructor(letterboxdLidWeb, letterboxdMemberApi, messageEmbedFactory) {
        this.letterboxdLidWeb = letterboxdLidWeb;
        this.letterboxdMemberApi = letterboxdMemberApi;
        this.messageEmbedFactory = messageEmbedFactory;
    }

    /**
     * @param {string} accountName
     * @returns {import('discord.js').MessageEmbed}
     */
    getMessage(accountName) {
        const searchMember = this.letterboxdLidWeb.get(accountName);
        const promiseList = [
            searchMember.then((lid) => this.letterboxdMemberApi.getMember(lid)),
            searchMember.then((lid) => this.letterboxdMemberApi.getMemberStatistics(lid)),
        ];

        return Promise.all(promiseList)
            .then(([member, memberStatistics]) =>
                this.messageEmbedFactory.createUserMessage(member, memberStatistics),
            )
            .catch(() => this.messageEmbedFactory.createNoAccountFoundMessage(accountName));
    }
}

module.exports = UserCommand;
