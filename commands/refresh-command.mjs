export default class RefreshCommand {
    /**
     * @param {import('../factories/embed-builder-factory.mjs')} embedBuilderFactory
     * @param {import('../services/google/firestore/firestore-user-dao.mjs')} firestoreUserDao
     * @param {import('../services/letterboxd/letterboxd-lid-web.mjs')} letterboxdLidWeb
     * @param {import('../services/letterboxd/api/letterboxd-member-api.mjs')} letterboxdMemberApi
     */
    constructor(embedBuilderFactory, firestoreUserDao, letterboxdLidWeb, letterboxdMemberApi) {
        this.embedBuilderFactory = embedBuilderFactory;
        this.firestoreUserDao = firestoreUserDao;
        this.letterboxdLidWeb = letterboxdLidWeb;
        this.letterboxdMemberApi = letterboxdMemberApi;
    }

    /**
     * @param {string} accountName
     * @returns {import('discord.js').EmbedBuilder}
     */
    process(accountName) {
        return this.letterboxdLidWeb
            .get(accountName)
            .then((userLid) => this.letterboxdMemberApi.getMember(userLid))
            .then((member) => {
                const name = member?.displayName;
                const image = member?.avatar.getSmallestImage();
                return this.firestoreUserDao.update(accountName, name, image);
            })
            .then((result) => this.embedBuilderFactory.createRefreshSuccessEmbed(result))
            .catch(() => this.embedBuilderFactory.createRefreshErrorEmbed(accountName));
    }
}
