export default class LetterboxdEntryApi {
    /**
     * @param {import('./letterboxd-api.mjs')} letterboxdApi
     * @param {import('../../../factories/letterboxd-entry-factory.mjs')} letterboxdEntryFactory
     */
    constructor(readonly letterboxdApi: any, readonly letterboxdEntryFactory: any) {}

    /**
     * @param {string} memberLetterboxdId
     * @param {number} quantity
     * @returns {Promise<import('../../../models/letterboxd/letterboxd-entry.mjs')[]>}
     */
    get(memberLetterboxdId: any, quantity: any) {
        return this.letterboxdApi
            .get('log-entries', {
                member: memberLetterboxdId,
                perPage: quantity,
            })
            .then((responseData: any) => {
                const entryDataList = responseData?.items;
                if (!entryDataList.length) {
                    return [];
                }
                return entryDataList.map((entryData: any) =>
                    this.letterboxdEntryFactory.buildFromObject(entryData),
                );
            });
    }
}
