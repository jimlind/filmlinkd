const ConfigFactory = require('./factories/config-factory');
const dotenv = require('dotenv');
const fs = require('fs');
const { ShardingManager } = require('discord.js');

// @ts-ignore TypeScript validation doesn't like json files as a module
const packageJson = require('./package.json');

// Load .env into process.env, set variables, create config
dotenv.config();
const environment = process.argv[2];
const configModel = new ConfigFactory(environment, process.env, packageJson, fs.existsSync).build();

const manager = new ShardingManager('./bot.js', {
    token: configModel.discordBotToken,
    totalShards: 2,
    shardArgs: [environment],
    respawn: false,
});
manager.on('shardCreate', (shard) => console.log(`Launched Shard ${shard.id}`));
manager.spawn();
