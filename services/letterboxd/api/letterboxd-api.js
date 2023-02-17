'use strict';

const crypto = require('crypto');
const uuid = require('uuid');

class LetterboxdApi {
    root = 'https://api.letterboxd.com/api/v0/';
    letterboxdApiSharedSecret = '';

    /**
     * @param {import('../../../models/config')} config
     * @param {import('../../http-client')} httpClient
     * @param {import('../../google/secret-manager')} secretManager
     */
    constructor(config, httpClient, secretManager) {
        this.config = config;
        this.httpClient = httpClient;
        this.secretManager = secretManager;
    }

    get(path, paramList) {
        const getLetterboxdApiSharedSecret = new Promise((resolve) => {
            if (this.letterboxdApiSharedSecret) {
                return resolve(this.letterboxdApiSharedSecret);
            }
            this.secretManager
                .getValue(this.config.letterboxdApiSharedSecretName)
                .then((letterboxdApiSharedSecret) => {
                    this.letterboxdApiSharedSecret = letterboxdApiSharedSecret;
                    resolve(letterboxdApiSharedSecret);
                });
        });

        getLetterboxdApiSharedSecret.then((letterboxdApiSharedSecret) => {
            const url = this.buildUrl(path, paramList);
            const signature = this.buildSignature('GET', url, letterboxdApiSharedSecret);
            const auth = { Authorization: `Signature ${signature}` };

            this.httpClient.headers = { ...this.httpClient.headers, ...auth };
            return this.httpClient.get(url, 10000).then((response) => {
                return response.status === 200 ? response.data : null;
            });
        });
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
