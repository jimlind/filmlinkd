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
     * @returns {Promise<Object>}
     */
    search(userName) {
        return this.letterboxdApi
            .get('search', {
                input: userName,
                include: 'MemberSearchItem',
                perPage: 1,
            })
            .then((responseData) => {
                const memberData = responseData?.items[0]?.member;
                if (!memberData) {
                    throw `Member not found for ${userName}`;
                }
                return this.letterboxdMemberFactory.buildFromObject(memberData);
            });
    }
}

module.exports = LetterboxdMemberApi;
