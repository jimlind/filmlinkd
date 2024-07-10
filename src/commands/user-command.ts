export default class UserCommand {
    /**
     * @param {import('../services/letterboxd/letterboxd-lid-web.mjs')} letterboxdLidWeb
     * @param {import('../services/letterboxd/api/letterboxd-member-api.mjs')} letterboxdMemberApi
     * @param {import('../factories/embed-builder-factory.mjs')} embedBuilderFactory
     */
    constructor(
        readonly letterboxdLidWeb: any,
        readonly letterboxdMemberApi: any,
        readonly embedBuilderFactory: any,
    ) {}

    /**
     * @param {string} accountName
     * @returns {import('discord.js').EmbedBuilder}
     */
    getEmbed(accountName: any) {
        const searchMember = this.letterboxdLidWeb.get(accountName);
        const promiseList = [
            searchMember.then((lid: any) => this.letterboxdMemberApi.getMember(lid)),
            searchMember.then((lid: any) => this.letterboxdMemberApi.getMemberStatistics(lid)),
        ];

        return Promise.all(promiseList)
            .then(([member, memberStatistics]) =>
                this.embedBuilderFactory.createUserEmbed(member, memberStatistics),
            )
            .catch(() => this.embedBuilderFactory.createNoAccountFoundEmbed(accountName));
    }
}
