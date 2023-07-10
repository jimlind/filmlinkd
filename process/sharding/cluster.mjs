import { SecretManagerServiceClient } from '@google-cloud/secret-manager';
import death from 'death';
import { ClusterManager } from 'discord-hybrid-sharding';
import SecretManager from '../../services/google/secret-manager.js';

export default class Cluster {
    /**
     * @param {import('convict').Config} config
     */
    constructor(config) {
        this.config = config;
        this.manager = new ClusterManager('./process/sharding/single.mjs');

        // Trigger clean up on task ending
        death(this.cleanUp.bind(this));
    }

    run() {
        this.manager.totalShards = 'auto';
        this.manager.shardsPerClusters = 2;
        this.manager.mode = 'process';

        // Setup the secret manager manually because we are avoiding the overhead of the
        // dependency injection container
        const keyFilename = this.config.get('googleCloudIdentityKeyFile');
        const secretManagerClient = new SecretManagerServiceClient({ keyFilename });
        const secretManager = new SecretManager(this.config, secretManagerClient);

        secretManager.getValue(this.config.get('discordBotTokenName')).then((token) => {
            this.manager.token = token;
            this.manager.spawn({ timeout: -1 }).catch((response) => {
                throw `Failed to Spawn ClusterManager: [${response.statusText}]`;
            });
        });
    }

    cleanUp() {
        // Force a process exit due to how the discord-hybrid-sharding library works
        process.exit();
    }
}
