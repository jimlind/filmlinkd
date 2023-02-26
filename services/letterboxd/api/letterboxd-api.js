const crypto = require('crypto');
const uuid = require('uuid');

class LetterboxdApi {
    /** @type {string} */
    root = 'https://api.letterboxd.com/api/v0/';
    /** @type {boolean} */
    secretLock = false;
    /** @type {string} */
    apiKey = '';
    /** @type {string} */
    apiSharedSecret = '';

    /**
     * @param {import('convict').Config} config
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
            if (this.secretLock) {
                const interval = setInterval(() => {
                    if (!this.secretLock) {
                        clearInterval(interval);
                        resolve({ key: this.apiKey, sharedSecret: this.apiSharedSecret });
                    }
                }, 250); // Mostly arbitrary timing of how long it takes on my dev environment
            } else {
                this.secretLock = true;

                Promise.all([
                    this.secretManager.getValue(this.config.get('letterboxdApiKeyName')),
                    this.secretManager.getValue(this.config.get('letterboxdApiSharedSecretName')),
                ]).then(([key, sharedSecret]) => {
                    this.apiKey = key;
                    this.apiSharedSecret = sharedSecret;
                    this.secretLock = false;

                    resolve({ key, sharedSecret });
                });
            }
        });

        return getLetterboxdApiSharedSecret.then(({ key, sharedSecret }) => {
            const url = this.buildUrl(path, paramList, key);
            const signature = this.buildSignature('GET', url, sharedSecret);
            const auth = { Authorization: `Signature ${signature}` };

            this.httpClient.headers = { ...this.httpClient.headers, ...auth };
            return this.httpClient.get(url, 10000).then((response) => {
                return response.status === 200 ? response.data : null;
            });
        });
    }

    buildUrl(path, paramList, key) {
        const url = new URL(this.root + path);

        // Add values to URL params for signature verification
        paramList.apikey = key;
        paramList.nonce = uuid.v4();
        paramList.timestamp = this.now();
        for (const key in paramList) {
            url.searchParams.set(key, paramList[key]);
        }

        return url.toString();
    }

    buildSignature(method, url, sharedSecret) {
        return crypto
            .createHmac('sha256', sharedSecret)
            .update([method.toUpperCase(), url, ''].join('\u0000'))
            .digest('hex')
            .toLowerCase();
    }

    now() {
        return Math.floor(Date.now() / 1000);
    }
}

module.exports = LetterboxdApi;
