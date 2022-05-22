class LetterboxdLogEntryApi {
    /**
     * @param {import('./letterboxd-api')} letterboxdApi
     * @param {import('../../../factories/letterboxd-log-entry-factory')} letterboxdLogEntryFactory
     */
    constructor(letterboxdApi, letterboxdLogEntryFactory) {
        this.letterboxdApi = letterboxdApi;
        this.letterboxdLogEntryFactory = letterboxdLogEntryFactory;
    }

    /**
     * @param {string} letterboxdId
     * @param {number} quantity
     * @returns {Promise<Object[]>}
     */
    getByMember(memberLetterboxdId, quantity) {
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
                    this.letterboxdLogEntryFactory.buildLogEntryFromObject(entryData),
                );
            });
    }

    /**
     * @param {string} letterboxdId
     * @param {string} filmLetterboxdId
     * @param {number} quantity
     * @returns {Promise<Object[]>}
     */
    getByMemberAndFilm(memberLetterboxdId, filmLetterboxdId, quantity) {
        return this.letterboxdApi
            .get('log-entries', {
                film: filmLetterboxdId,
                member: memberLetterboxdId,
                memberRelationship: 'Owner',
                perPage: quantity,
            })
            .then((responseData) => {
                const entryDataList = responseData?.items;
                if (!entryDataList.length) {
                    return [];
                }
                return entryDataList.map((entryData) =>
                    this.letterboxdLogEntryFactory.buildLogEntryFromObject(entryData),
                );
            });
    }
}

module.exports = LetterboxdLogEntryApi;
