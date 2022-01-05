class ListCommand {
    /**
     * @param {import('../services/letterboxd/letterboxd-lid-web')} letterboxdLidWeb
     * @param {import('../services/letterboxd/api/letterboxd-list-api')} letterboxdListApi
     * @param {import('../factories/message-embed-factory')} messageEmbedFactory
     */
    constructor(letterboxdLidWeb, letterboxdListApi, messageEmbedFactory) {
        this.letterboxdLidWeb = letterboxdLidWeb;
        this.letterboxdListApi = letterboxdListApi;
        this.messageEmbedFactory = messageEmbedFactory;
    }

    /**
     * @param {string} accountName
     * @param {string} listName
     * @returns {import('discord.js').MessageEmbed}
     */
    getMessage(accountName, listName) {
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
                return this.messageEmbedFactory.createListMessage(selectedList[0]);
            })
            .catch(() => this.messageEmbedFactory.createNoListFoundMessage());
    }
}

module.exports = ListCommand;
