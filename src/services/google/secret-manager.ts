export default class SecretManager {
    /**
     * @type {{}}
     */
    cachedData = {} as { name: string };

    /**
     * @param {import('convict').Config} config
     * @param {import('@google-cloud/secret-manager').v1.SecretManagerServiceClient} secretManagerClient
     */
    constructor(readonly config: any, readonly secretManagerClient: any) {}

    /**
     * @param {string} secretName
     * @returns {Promise<string>}
     */
    getValue(secretName: any): any {
        const projectName = this.config.get('googleCloudProjectId');
        const name = `projects/${projectName}/secrets/${secretName}/versions/latest`;

        if (name in this.cachedData) {
            return new Promise((resolve) => {
                resolve(this.cachedData.name);
            });
        }

        return this.secretManagerClient.accessSecretVersion({ name }).then(([response]: any) => {
            const responseString = response.payload.data.toString('utf8');
            this.cachedData.name = responseString;

            return responseString;
        });
    }
}
