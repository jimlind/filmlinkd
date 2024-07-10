export default class SecretManager {
    /**
     * @type {{}}
     */
    cachedData = {};

    /**
     * @param {import('convict').Config} config
     * @param {import('@google-cloud/secret-manager').v1.SecretManagerServiceClient} secretManagerClient
     */
    constructor(config, secretManagerClient) {
        this.config = config;
        this.secretManagerClient = secretManagerClient;
    }

    /**
     * @param {string} secretName
     * @returns {Promise<string>}
     */
    getValue(secretName) {
        const projectName = this.config.get('googleCloudProjectId');
        const name = `projects/${projectName}/secrets/${secretName}/versions/latest`;

        if (name in this.cachedData) {
            return new Promise((resolve) => {
                resolve(this.cachedData[name]);
            });
        }

        return this.secretManagerClient.accessSecretVersion({ name }).then(([response]) => {
            const responseString = response.payload.data.toString('utf8');
            this.cachedData[name] = responseString;

            return responseString;
        });
    }
}
