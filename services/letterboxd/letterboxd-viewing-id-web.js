'use strict';

class LetterboxdViewingIdWeb {
    /**
     * @param {import('../http-client')} httpClient
     */
    constructor(httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * @param {string} url
     * @returns {Promise<string>}
     */
    get(url) {
        return this.httpClient.head(url, 10000).then((response) => {
            const xkey = response?.headers['xkey'] || '';

            return xkey.match(/viewing:(\d+)\//)?.[1] || '';
        });
    }
}

module.exports = LetterboxdViewingIdWeb;
