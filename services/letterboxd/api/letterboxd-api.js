'use strict';

const crypto = require('crypto');
const uuid = require('uuid');

class LetterboxdApi {
    root = 'https://api.letterboxd.com/api/v0/';

    /**
     * @param {import('../../../models/config')} config
     * @param {import('../../http-client')} httpClient
     */
    constructor(config, httpClient) {
        this.config = config;
        this.httpClient = httpClient;
    }

    get(path, paramList) {
        const url = this.buildUrl(path, paramList);
        const signature = this.buildSignature('GET', url);

        // TODO: Move this header setting into the 'get' method probably.
        this.httpClient.headers = {
            ...this.httpClient.headers,
            Authorization: `Signature ${signature}`,
        };

        return this.httpClient
            .get(url, 1000)
            .then((response) => (response.status === 200 ? response.data : null))
            .catch(() => null);
    }

    buildUrl(path, paramList) {
        const url = new URL(this.root + path);

        // Add values to URL params for signature verification
        paramList.apikey = this.config.letterboxdApiKey;
        paramList.nonce = uuid.v4();
        paramList.timestamp = this.now();
        for (const key in paramList) {
            url.searchParams.set(key, paramList[key]);
        }

        return url.toString();
    }

    buildSignature(method, url) {
        return crypto
            .createHmac('sha256', this.config.letterboxdApiSharedSecret)
            .update([method.toUpperCase(), url, ''].join('\u0000'))
            .digest('hex')
            .toLowerCase();
    }

    now() {
        return Math.floor(Date.now() / 1000);
    }
}

module.exports = LetterboxdApi;
