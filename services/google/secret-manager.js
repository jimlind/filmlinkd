'use strict';

class SecretManager {
    /**
     * @param {import('../../models/config')} config
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
        const name = `projects/${this.config.googleCloudProjectId}/secrets/${secretName}/versions/latest`;
        return this.secretManagerClient
            .accessSecretVersion({ name })
            .then(([response]) => response.payload.data.toString('utf8'));
    }
}

module.exports = SecretManager;
