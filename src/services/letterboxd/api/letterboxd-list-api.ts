export default class LetterboxdListApi {
    /**
     * @param {import('./letterboxd-api.mjs')} letterboxdApi
     * @param {import('../../../factories/letterboxd/letterboxd-list-factory.mjs')} letterboxdListFactory
     */
    constructor(readonly letterboxdApi: any, readonly letterboxdListFactory: any) {}

    /**
     * @param {string} letterboxdId
     * @param {number} quantity
     * @returns {Promise<import('../../../models/letterboxd/letterboxd-list-summary.mjs')[]>}
     */
    getMembersLists(letterboxdId: any, quantity: any) {
        return this.letterboxdApi
            .get('lists', {
                member: letterboxdId,
                memberRelationship: 'Owner',
                perPage: quantity,
                where: 'Published',
            })
            .then((responseData: any) => {
                return (responseData?.items || []).map((listData: any) =>
                    this.letterboxdListFactory.buildListSummaryFromObject(listData),
                );
            });
    }
}
