const { ClusterManager } = require('discord-hybrid-sharding');
const { SecretManagerServiceClient } = require('@google-cloud/secret-manager').v1;
const SecretManager = require('../../services/google/secret-manager');

class Cluster {
    /**
     * @param {import('convict').Config} config
     */
    constructor(config) {
        this.config = config;
        this.manager = new ClusterManager(`${__dirname}/single.js`);

        // Trigger clean up on task ending
        require('death')(this.cleanUp.bind(this));
    }

    run() {
        this.manager.totalShards = 'auto';
        this.manager.shardsPerClusters = 2;
        this.manager.mode = 'process';

        // Setup the secret manager manually because we are avoiding the overhead of the
        // dependency injection container
        const keyFilename = this.config.get('gcpKeyFile');
        const secretManagerClient = new SecretManagerServiceClient({ keyFilename });
        const secretManager = new SecretManager(this.config, secretManagerClient);

        secretManager.getValue(this.config.get('discordBotTokenName')).then((token) => {
            this.manager.token = token;
            this.manager.spawn({ timeout: -1 });
        });
    }

    cleanUp() {
        // Nullify all manager attributes so it can close out safely
        this.manager.totalClusters = 0;
        this.manager.totalShards = 0;
        this.manager.respawn = false;
    }
}

module.exports = Cluster;
