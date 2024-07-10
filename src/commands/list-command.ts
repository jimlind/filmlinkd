export default class ListCommand {
    /**
     * @param {import('../services/letterboxd/letterboxd-lid-web.mjs')} letterboxdLidWeb
     * @param {import('../services/letterboxd/api/letterboxd-list-api.mjs')} letterboxdListApi
     * @param {import('../factories/embed-builder-factory.mjs')} embedBuilderFactory
     */
    constructor(
        readonly letterboxdLidWeb: any,
        readonly letterboxdListApi: any,
        readonly embedBuilderFactory: any,
    ) {}

    /**
     * @param {string} accountName
     * @param {string} listName
     * @returns {import('discord.js').EmbedBuilder}
     */
    getEmbed(accountName: any, listName: any) {
        const cleanListName = listName.toLocaleLowerCase().replace(/[^a-z0-9]/g, '');

        return this.letterboxdLidWeb
            .get(accountName)
            .then((lid: any) => this.letterboxdListApi.getMembersLists(lid, 50))
            .then((lists: any) => {
                const selectedList = lists.filter((list: any) => {
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
