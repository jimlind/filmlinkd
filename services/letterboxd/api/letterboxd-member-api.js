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
                perPage: 4,
            })
            .then((responseData) => {
                if (!responseData?.items?.length) {
                    throw `Member Not Found For ${userName}. (Type 1)`;
                }
                const filteredMemberList = responseData?.items.filter((data) => {
                    return data.member.username.toLowerCase() == userName;
                });
                if (!filteredMemberList.length) {
                    throw `Member not found for ${userName} (Type 2)`;
                }
                return this.letterboxdMemberFactory.buildFromObject(filteredMemberList[0].member);
            });
    }
}

module.exports = LetterboxdMemberApi;
