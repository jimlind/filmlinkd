'use strict';

class LetterboxdUserApi {
    /**
     * @param {import('./letterboxd-api')} letterboxdApi
     */
    constructor(letterboxdApi) {
        this.letterboxdApi = letterboxdApi;
    }

    get(userName) {
        return this.letterboxdApi
            .get('search', {
                input: userName,
                include: 'MemberSearchItem',
                perPage: 1,
            })
            .then((responseData) => {
                return responseData.items[0].member;
            });
    }
}

module.exports = LetterboxdUserApi;
