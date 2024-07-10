export default class LetterboxdLidWeb {
    /**
     * @param {import('../http-client.mjs')} httpClient
     */
    constructor(readonly httpClient: any) {}

    /**
     * Unfortunatly I named this method and created docs for it before I discovered what it could be used for.
     * It should be refactored to a clearer name.
     *
     * @param {string} userName
     * @returns {Promise<string>}
     */
    get(userName: any) {
        return this.getFromUrl('https://letterboxd.com/' + userName);
    }

    /**
     * @param {string} path
     * @returns {Promise<string>}
     */
    getFromPath(path: any) {
        return this.getFromUrl('https://letterboxd.com' + path);
    }

    /**
     * @param {string} url
     * @returns {Promise<string>}
     */
    getFromUrl(url: any) {
        return this.httpClient.head(url, 10000).then((response: any) => {
            const letterboxdId = response?.headers['x-letterboxd-identifier'] || '';
            if (!letterboxdId) {
                throw `Letterboxd Id Not Found on ${url}`;
            }

            return letterboxdId;
        });
    }
}
