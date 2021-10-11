'use strict';

class HttpClient {
    /**
     * @param {import('axios').default} axios - Library for downloading
     */
    constructor(axios) {
        this.axios = axios;
    }

    /**
     * @param {string} url
     * @param {number} timeout
     */
    get(url, timeout) {
        const abort = this.axios.CancelToken.source();
        const timeoutId = setTimeout(() => abort.cancel(), timeout);

        return this.axios.get(url, { cancelToken: abort.token }).then((response) => {
            clearTimeout(timeoutId);
            return response;
        });
    }
}

module.exports = HttpClient;
