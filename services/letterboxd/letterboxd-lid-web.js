'use strict';

class LetterboxdLidWeb {
    /**
     * @param {import('../http-client')} httpClient
     */
    constructor(httpClient) {
        this.httpClient = httpClient;
    }

    get(userName) {
        const url = 'https://letterboxd.com/' + userName;

        return this.httpClient
            .head(url, 1000)
            .then((response) => {
                return response?.headers['x-letterboxd-identifier'] || '';
            })
            .catch(() => {
                return '';
            });
    }
}

module.exports = LetterboxdLidWeb;
