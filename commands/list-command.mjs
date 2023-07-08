export default class ListCommand {
    /**
     * @param {import('../services/letterboxd/letterboxd-lid-web')} letterboxdLidWeb
     * @param {import('../services/letterboxd/api/letterboxd-list-api')} letterboxdListApi
     * @param {import('../factories/embed-builder-factory')} embedBuilderFactory
     */
    constructor(letterboxdLidWeb, letterboxdListApi, embedBuilderFactory) {
        this.letterboxdLidWeb = letterboxdLidWeb;
        this.letterboxdListApi = letterboxdListApi;
        this.embedBuilderFactory = embedBuilderFactory;
    }

    /**
     * @param {string} accountName
     * @param {string} listName
     * @returns {import('discord.js').EmbedBuilder}
     */
    getEmbed(accountName, listName) {
        const cleanListName = listName.toLocaleLowerCase().replace(/[^a-z0-9]/g, '');

        return this.letterboxdLidWeb
            .get(accountName)
            .then((lid) => this.letterboxdListApi.getMembersLists(lid, 50))
            .then((lists) => {
                const selectedList = lists.filter((list) => {
                    return cleanListName == list.name.toLocaleLowerCase().replace(/[^a-z0-9]/g, '');
                });
                if (selectedList.length == 0) {
                    throw `List:"${listName}" for Account:"${accountName}" can't be found.`;
                }
                return this.embedBuilderFactory.createListEmbed(selectedList[0]);
            })
            .catch(() => this.embedBuilderFactory.createNoListFoundEmbed());
    }
}
