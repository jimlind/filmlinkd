export default class LetterboxdViewingIdWeb {
    /**
     * @param {import('../http-client.mjs')} httpClient
     */
    constructor(httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * @param {string} url
     * @returns {Promise<string>}
     */
    get(url) {
        return this.httpClient.head(url, 100000).then((response) => {
            const xkey = response?.headers['xkey'] || '';

            return xkey.match(/viewing:(\d+)\//)?.[1] || '';
        });
    }
}
