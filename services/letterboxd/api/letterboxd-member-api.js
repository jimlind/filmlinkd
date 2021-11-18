'use strict';

class LetterboxdMemberApi {
    /**
     * @param {import('./letterboxd-api')} letterboxdApi
     * @param {import('../../../factories/letterboxd-member-factory')} letterboxdMemberFactory
     */
    constructor(letterboxdApi, letterboxdMemberFactory) {
        this.letterboxdApi = letterboxdApi;
        this.letterboxdMemberFactory = letterboxdMemberFactory;
    }

    /**
     * @param {string} userName
     * @returns {Promise<Object | null>}
     */
    get(userName) {
        return this.letterboxdApi
            .get('search', {
                input: userName,
                include: 'MemberSearchItem',
                perPage: 1,
            })
            .then((responseData) => {
                const memberData = responseData?.items[0]?.member;
                if (memberData) {
                    return this.letterboxdMemberFactory.buildFromObject(memberData);
                }
                return null;
            });
    }
}

module.exports = LetterboxdMemberApi;
