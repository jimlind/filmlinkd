#!/usr/bin/env node

const ConfigFactory = require('../factories/config-factory');
const DependencyInjectionContainer = require('../dependency-injection-container');
const dotenv = require('dotenv');
const fs = require('fs');

// Load .env into process.env, create config, create container
dotenv.config();
const configModel = new ConfigFactory('dev', process.env, [], fs.existsSync).build();
const container = new DependencyInjectionContainer(configModel);

go();

async function go() {
    const output = await container
        .resolve('followCommand')
        .process('protolexus', 799785154032959528);
}
