'use strict';

//const DiaryEntry = require('../../../models/diary-entry');

class LetterboxdEntryApi {
    /**
     * @param {import('./letterboxd-api')} letterboxdApi
     * @param {import('../../../factories/letterboxd-entry-factory')} letterboxdEntryFactory
     */
    constructor(letterboxdApi, letterboxdEntryFactory) {
        this.letterboxdApi = letterboxdApi;
        this.letterboxdEntryFactory = letterboxdEntryFactory;
    }

    /**
     * @param {string} letterboxdId
     * @param {number} quantity
     * @returns {Promise<Object>}
     */
    get(letterboxdId, quantity) {
        return this.letterboxdApi
            .get('log-entries', {
                member: letterboxdId,
                perPage: quantity,
            })
            .then((responseData) => {
                const entryDataList = responseData?.items;
                if (!entryDataList.length) {
                    return {};
                }
                return entryDataList.map((entryData) =>
                    this.letterboxdEntryFactory.buildFromObject(entryData),
                );
            });
    }
}

module.exports = LetterboxdEntryApi;
