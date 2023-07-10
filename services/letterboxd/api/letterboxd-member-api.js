'use strict';

class LetterboxdMemberApi {
    /**
     * @param {import('./letterboxd-api')} letterboxdApi
     * @param {import('../../../factories/letterboxd/letterboxd-member-factory.mjs')} letterboxdMemberFactory
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

                const memberObject = filteredMemberList[0].member;
                return this.letterboxdMemberFactory.buildMemberFromObject(memberObject);
            });
    }

    /**
     * @param {string} letterboxdId
     * @returns {Promise<import('../../../models/letterboxd/letterboxd-member.mjs')>}
     */
    getMember(letterboxdId) {
        return this.letterboxdApi
            .get(`member/${letterboxdId}`, {})
            .then((responseData) =>
                this.letterboxdMemberFactory.buildMemberFromObject(responseData),
            );
    }

    /**
     * @param {string} letterboxdId
     * @returns {Promise<import('../../../models/letterboxd/letterboxd-member-statistics.mjs')>}
     */
    getMemberStatistics(letterboxdId) {
        return this.letterboxdApi
            .get(`member/${letterboxdId}/statistics`, {})
            .then((responseData) =>
                this.letterboxdMemberFactory.buildMemberStatisticsFromObject(responseData),
            );
    }
}

module.exports = LetterboxdMemberApi;
