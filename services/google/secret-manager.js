'use strict';

class SecretManager {
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

        return this.secretManagerClient
            .accessSecretVersion({ name })
            .then(([response]) => response.payload.data.toString('utf8'));
    }
}

module.exports = SecretManager;
