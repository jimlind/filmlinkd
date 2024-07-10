export default class LetterboxdMemberApi {
    /**
     * @param {import('./letterboxd-api.mjs')} letterboxdApi
     * @param {import('../../../factories/letterboxd/letterboxd-member-factory.mjs')} letterboxdMemberFactory
     */
    constructor(readonly letterboxdApi: any, readonly letterboxdMemberFactory: any) {}

    /**
     * @param {string} userName
     * @returns {Promise<Object>}
     */
    search(userName: any) {
        return this.letterboxdApi
            .get('search', {
                input: userName,
                include: 'MemberSearchItem',
                perPage: 4,
            })
            .then((responseData: any) => {
                if (!responseData?.items?.length) {
                    throw `Member Not Found For ${userName}. (Type 1)`;
                }
                const filteredMemberList = responseData?.items.filter((data: any) => {
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
    getMember(letterboxdId: any) {
        return this.letterboxdApi
            .get(`member/${letterboxdId}`, {})
            .then((responseData: any) =>
                this.letterboxdMemberFactory.buildMemberFromObject(responseData),
            );
    }

    /**
     * @param {string} letterboxdId
     * @returns {Promise<import('../../../models/letterboxd/letterboxd-member-statistics.mjs')>}
     */
    getMemberStatistics(letterboxdId: any) {
        return this.letterboxdApi
            .get(`member/${letterboxdId}/statistics`, {})
            .then((responseData: any) =>
                this.letterboxdMemberFactory.buildMemberStatisticsFromObject(responseData),
            );
    }
}
