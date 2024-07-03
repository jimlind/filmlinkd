import { SecretManagerServiceClient } from '@google-cloud/secret-manager';
import { ShardingManager } from 'discord.js';
import SecretManager from '../../services/google/secret-manager.mjs';

export default class Manager {
    /**
     * @param {import('convict').Config} config
     */
    constructor(config) {
        this.config = config;
    }

    async run() {
        // Setup the secret manager manually because we are avoiding the overhead of the
        // dependency injection container on this manager when every shard will have thier own
        // independent collection of dependencies as well
        const keyFilename = this.config.get('googleCloudIdentityKeyFile');
        const secretManagerClient = new SecretManagerServiceClient({ keyFilename });
        const secretManager = new SecretManager(this.config, secretManagerClient);

        const token = await secretManager.getValue(this.config.get('discordBotTokenName'));
        const manager = new ShardingManager('./process/sharding/shard.mjs', {
            token,
            respawn: true,
            mode: 'process',
        });
        manager.spawn();
    }
}
