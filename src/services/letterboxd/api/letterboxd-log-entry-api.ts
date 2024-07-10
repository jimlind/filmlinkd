export default class LetterboxdLogEntryApi {
    /**
     * @param {import('./letterboxd-api.mjs')} letterboxdApi
     * @param {import('../../../factories/letterboxd-log-entry-factory')} letterboxdLogEntryFactory
     */
    constructor(readonly letterboxdApi: any, readonly letterboxdLogEntryFactory: any) {}

    /**
     * @param {string} letterboxdId
     * @param {number} quantity
     * @returns {Promise<Object[]>}
     */
    getByMember(memberLetterboxdId: any, quantity: any) {
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
    getByMemberAndFilm(memberLetterboxdId: any, filmLetterboxdId: any, quantity: any) {
        return this.letterboxdApi
            .get('log-entries', {
                film: filmLetterboxdId,
                member: memberLetterboxdId,
                memberRelationship: 'Owner',
                perPage: quantity,
            })
            .then((responseData: any) => {
                const entryDataList = responseData?.items;
                if (!entryDataList.length) {
                    return [];
                }
                return entryDataList.map((entryData: any) =>
                    this.letterboxdLogEntryFactory.buildLogEntryFromObject(entryData),
                );
            });
    }
}
