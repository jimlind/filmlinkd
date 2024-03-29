export default class LetterboxdEntryApi {
    /**
     * @param {import('./letterboxd-api.mjs')} letterboxdApi
     * @param {import('../../../factories/letterboxd-entry-factory.mjs')} letterboxdEntryFactory
     */
    constructor(letterboxdApi, letterboxdEntryFactory) {
        this.letterboxdApi = letterboxdApi;
        this.letterboxdEntryFactory = letterboxdEntryFactory;
    }

    /**
     * @param {string} memberLetterboxdId
     * @param {number} quantity
     * @returns {Promise<import('../../../models/letterboxd/letterboxd-entry.mjs')[]>}
     */
    get(memberLetterboxdId, quantity) {
        return this.letterboxdApi
            .get('log-entries', {
                member: memberLetterboxdId,
                perPage: quantity,
            })
            .then((responseData) => {
                const entryDataList = responseData?.items;
                if (!entryDataList.length) {
                    return [];
                }
                return entryDataList.map((entryData) =>
                    this.letterboxdEntryFactory.buildFromObject(entryData),
                );
            });
    }
}
