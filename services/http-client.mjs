export default class HttpClient {
    headers = { 'User-Agent': 'Filmlinkd - A Letterboxd Discord Bot' };

    /**
     * @param {import('axios').default} axios - Library for downloading
     */
    constructor(axios) {
        this.axios = axios;
    }

    /**
     * @param {string} url
     * @param {number} timeout
     *
     * @returns {Promise<import('axios').AxiosResponse>}
     */
    get(url, timeout) {
        const abort = this.axios.CancelToken.source();
        const timeoutId = setTimeout(() => abort.cancel(), timeout);

        return this.axios
            .get(url, { cancelToken: abort.token, headers: this.headers })
            .then((response) => {
                clearTimeout(timeoutId);
                return response;
            });
    }

    /**
     * @param {string} url
     * @param {number} timeout
     *
     * @returns {Promise<import('axios').AxiosResponse>}
     */
    head(url, timeout) {
        const abort = this.axios.CancelToken.source();
        const timeoutId = setTimeout(() => abort.cancel(), timeout);

        // Don't make a full get request when a head request will do
        return this.axios
            .head(url, { cancelToken: abort.token, headers: this.headers })
            .then((response) => {
                clearTimeout(timeoutId);
                return response;
            });
    }
}
