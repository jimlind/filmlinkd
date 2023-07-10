class LetterboxdListApi {
    /**
     * @param {import('./letterboxd-api')} letterboxdApi
     * @param {import('../../../factories/letterboxd/letterboxd-list-factory.mjs')} letterboxdListFactory
     */
    constructor(letterboxdApi, letterboxdListFactory) {
        this.letterboxdApi = letterboxdApi;
        this.letterboxdListFactory = letterboxdListFactory;
    }

    /**
     * @param {string} letterboxdId
     * @param {number} quantity
     * @returns {Promise<import('../../../models/letterboxd/letterboxd-list-summary.mjs')[]>}
     */
    getMembersLists(letterboxdId, quantity) {
        return this.letterboxdApi
            .get('lists', {
                member: letterboxdId,
                memberRelationship: 'Owner',
                perPage: quantity,
                where: 'Published',
            })
            .then((responseData) => {
                return (responseData?.items || []).map((listData) =>
                    this.letterboxdListFactory.buildListSummaryFromObject(listData),
                );
            });
    }
}

module.exports = LetterboxdListApi;
