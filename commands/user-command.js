class UserCommand {
    /**
     * @param {import('../services/letterboxd/letterboxd-lid-web')} letterboxdLidWeb
     * @param {import('../services/letterboxd/api/letterboxd-member-api')} letterboxdMemberApi
     * @param {import('../factories/embed-builder-factory')} embedBuilderFactory
     */
    constructor(letterboxdLidWeb, letterboxdMemberApi, embedBuilderFactory) {
        this.letterboxdLidWeb = letterboxdLidWeb;
        this.letterboxdMemberApi = letterboxdMemberApi;
        this.embedBuilderFactory = embedBuilderFactory;
    }

    /**
     * @param {string} accountName
     * @returns {import('discord.js').EmbedBuilder}
     */
    getMessage(accountName) {
        const searchMember = this.letterboxdLidWeb.get(accountName);
        const promiseList = [
            searchMember.then((lid) => this.letterboxdMemberApi.getMember(lid)),
            searchMember.then((lid) => this.letterboxdMemberApi.getMemberStatistics(lid)),
        ];

        return Promise.all(promiseList)
            .then(([member, memberStatistics]) =>
                this.embedBuilderFactory.createUserEmbed(member, memberStatistics),
            )
            .catch(() => this.embedBuilderFactory.createNoAccountFoundEmbed(accountName));
    }
}

module.exports = UserCommand;
