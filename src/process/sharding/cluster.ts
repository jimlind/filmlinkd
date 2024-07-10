import { SecretManagerServiceClient } from '@google-cloud/secret-manager';
import { ClusterManager } from 'discord-hybrid-sharding';
import exitHook from 'exit-hook';
import SecretManager from '../../services/google/secret-manager.js';
export default class Cluster {
    manager: any;

    /**
     * @param {import('convict').Config} config
     */
    constructor(readonly config: any) {
        this.manager = new ClusterManager('./dist/process/sharding/single.js', {});

        // Trigger clean up on task ending
        exitHook(this.cleanUp.bind(this));
    }

    run() {
        // A cluster is a collection of shards. Clusters are actual divisions. Shards are virtual
        // divisions.
        // A cluster of shards acts like one sever. Multiple clusters require additional engineering to
        // resolve any need to share events or data between them.
        // Setting shards to 'auto' means the sharding attempts to create shards with 1,000 servers.
        // totalShards:'auto' and shardsPerClusters:4 and 3,999 servers means 1 cluster with 4 shards
        // totalShards:'auto' and shardsPerClusters:2 and 3,999 servers means 2 clusters each with 2 shards
        this.manager.totalShards = 'auto';
        this.manager.shardsPerClusters = 12;
        this.manager.mode = 'process';

        // Setup the secret manager manually because we are avoiding the overhead of the
        // dependency injection container
        const keyFilename = this.config.get('googleCloudIdentityKeyFile');
        const secretManagerClient = new SecretManagerServiceClient({ keyFilename });
        const secretManager = new SecretManager(this.config, secretManagerClient);

        secretManager.getValue(this.config.get('discordBotTokenName')).then((token: any) => {
            this.manager.token = token;
            this.manager.spawn({ timeout: -1 }).catch((response: { statusText: any }) => {
                throw `Failed to Spawn ClusterManager: [${response.statusText}]`;
            });
        });
    }

    cleanUp() {
        // Force a process exit due to how the discord-hybrid-sharding library works
        process.exit();
    }
}
