import { SecretManagerServiceClient } from '@google-cloud/secret-manager';
import { ShardingManager } from 'discord.js';
import container from '../../dependency-injection-container.mjs';
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
            respawn: false,
            mode: 'process',
        });

        const initializedContainer = await container(this.config).initialize();
        const logger = initializedContainer.resolve('logger');

        manager.on('shardCreate', (shard) => {
            shard.on('reconnecting', () => {
                logger.info(`Reconnecting shard: [${shard.id}]`);
            });
            shard.on('spawn', () => {
                logger.info(`Spawned shard: [${shard.id}]`);
            });
            shard.on('ready', () => {
                logger.info(` Shard [${shard.id}] is ready`);
            });
            shard.on('death', () => {
                logger.info(`Died shard: [${shard.id}]`);
            });
            shard.on('error', (err) => {
                logger.info(`Error in  [${shard.id}] with : ${err} `);
                shard.respawn();
            });
        });
        manager.spawn();
    }
}
