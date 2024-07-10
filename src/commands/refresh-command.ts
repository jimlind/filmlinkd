export default class RefreshCommand {
    /**
     * @param {import('../factories/embed-builder-factory.mjs')} embedBuilderFactory
     * @param {import('../services/google/firestore/firestore-user-dao.mjs')} firestoreUserDao
     * @param {import('../services/letterboxd/letterboxd-lid-web.mjs')} letterboxdLidWeb
     * @param {import('../services/letterboxd/api/letterboxd-member-api.mjs')} letterboxdMemberApi
     */
    constructor(
        readonly embedBuilderFactory: any,
        readonly firestoreUserDao: any,
        readonly letterboxdLidWeb: any,
        readonly letterboxdMemberApi: any,
    ) {}

    /**
     * @param {string} accountName
     * @returns {import('discord.js').EmbedBuilder}
     */
    process(accountName: any) {
        return this.letterboxdLidWeb
            .get(accountName)
            .then((userLid: any) => this.letterboxdMemberApi.getMember(userLid))
            .then((member: any) => {
                return this.firestoreUserDao.updateByLetterboxdId(
                    member.id,
                    member.userName,
                    member.displayName,
                    member.avatar.getSmallestImage(),
                );
            })
            .then((result: any) => this.embedBuilderFactory.createRefreshSuccessEmbed(result))
            .catch(() => this.embedBuilderFactory.createRefreshErrorEmbed(accountName));
    }
}
