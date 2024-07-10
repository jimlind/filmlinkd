export default class LetterboxdApi {
    /** @type {string} */
    root = 'https://api.letterboxd.com/api/v0/';
    /** @type {boolean} */
    secretValueGetterLock = false;
    /** @type {string} */
    apiKey = '';
    /** @type {string} */
    apiSharedSecret = '';

    /**
     * @param {import('convict').Config} config
     * @param {*} crypto
     * @param {import('../../http-client.mjs')} httpClient
     * @param {import('../../google/secret-manager.mjs')} secretManager
     * @param {*} uuid
     */
    constructor(
        readonly config: any,
        readonly crypto: any,
        readonly httpClient: any,
        readonly secretManager: any,
        readonly uuid: any,
    ) {}

    get(path: any, paramList: any) {
        const getLetterboxdApiSecrets = new Promise((resolve) => {
            // If the secrets are set locally use them
            if (this.apiKey && this.apiSharedSecret) {
                return resolve({ key: this.apiKey, sharedSecret: this.apiSharedSecret });
            }

            // If the getter is locked start some recursive timeouts to resolve it
            if (this.secretValueGetterLock) {
                // Mostly arbitrary timing of how long it takes on my dev environment
                const delay = 250;
                const getSecretValues = () => {
                    if (this.apiKey && this.apiSharedSecret) {
                        resolve({ key: this.apiKey, sharedSecret: this.apiSharedSecret });
                    } else {
                        setTimeout(getSecretValues, delay);
                    }
                };
                setTimeout(getSecretValues, delay);
            } else {
                this.secretValueGetterLock = true;

                Promise.all([
                    this.secretManager.getValue(this.config.get('letterboxdApiKeyName')),
                    this.secretManager.getValue(this.config.get('letterboxdApiSharedSecretName')),
                ]).then(([key, sharedSecret]) => {
                    this.apiKey = key;
                    this.apiSharedSecret = sharedSecret;

                    resolve({ key, sharedSecret });
                });
            }
        });

        return getLetterboxdApiSecrets.then(({ key, sharedSecret }: any) => {
            const url = this.buildUrl(path, paramList, key);
            const signature = this.buildSignature('GET', url, sharedSecret);
            const auth = { Authorization: `Signature ${signature}` };

            this.httpClient.headers = { ...this.httpClient.headers, ...auth };
            return this.httpClient.get(url, 10000).then((response: any) => {
                return response.status === 200 ? response.data : null;
            });
        });
    }

    buildUrl(path: any, paramList: any, key: any) {
        const url = new URL(this.root + path);

        // Add values to URL params for signature verification
        paramList.apikey = key;
        paramList.nonce = this.uuid.v4();
        paramList.timestamp = this.now();
        for (const key in paramList) {
            url.searchParams.set(key, paramList[key]);
        }

        return url.toString();
    }

    buildSignature(method: any, url: any, sharedSecret: any) {
        return this.crypto
            .createHmac('sha256', sharedSecret)
            .update([method.toUpperCase(), url, ''].join('\u0000'))
            .digest('hex')
            .toLowerCase();
    }

    now() {
        return Math.floor(Date.now() / 1000);
    }
}
