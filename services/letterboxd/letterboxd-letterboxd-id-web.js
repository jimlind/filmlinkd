'use strict';

class LetterboxdLidWeb {
    /**
     * @param {import('../http-client')} httpClient
     */
    constructor(httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * @param {string} userName
     * @returns {Promise<string>}
     */
    get(userName) {
        const url = 'https://letterboxd.com/' + userName;

        return this.httpClient.head(url, 10000).then((response) => {
            const letterboxdId = response?.headers['x-letterboxd-identifier'] || '';
            if (!letterboxdId) {
                throw `Letterboxd Id Not Found on ${url}`;
            }

            return letterboxdId;
        });
    }
}

module.exports = LetterboxdLidWeb;
