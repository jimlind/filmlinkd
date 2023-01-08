const ConfigFactory = require('./factories/config-factory');
const DependencyInjectionContainer = require('./dependency-injection-container');
const dotenv = require('dotenv');
const fs = require('fs');
const { ShardingManager } = require('discord.js');

// @ts-ignore TypeScript validation doesn't like json files as a module
const packageJson = require('./package.json');

// Load .env into process.env, set variables, create config
dotenv.config();
const environment = process.argv[2];
const configModel = new ConfigFactory(environment, process.env, packageJson, fs.existsSync).build();
const container = new DependencyInjectionContainer(configModel);

const manager = new ShardingManager('./bot.js', {
    token: configModel.discordBotToken,
    totalShards: 2,
    shardArgs: [environment],
    respawn: false,
});
manager.on('shardCreate', (shard) => {
    container.resolve('logger').info(`Launched Shard ${shard.id}`);
});
manager.spawn();

// Kill this process after 4 hours
const startTime = Date.now();
setInterval(() => {
    if (Date.now() > startTime + 4 * 60 * 60000) {
        container.resolve('logger').info('4 Hour Reset');
        manager.shards.forEach((shard) => shard.kill());
        return process.exit();
    }
}, 5 * 60 * 1000);
