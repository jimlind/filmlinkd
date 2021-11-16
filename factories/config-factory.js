'use strict';

const Config = require('../models/config');

class ConfigFactory {
    constructor(argument, environment, packageJson, fileExists) {
        this.argument = argument;
        this.environment = environment;
        this.packageJson = packageJson;
        this.fileExists = fileExists;
    }

    build() {
        const config = new Config();

        config.packageName = this.packageJson?.name || '';
        config.packageVersion = this.packageJson?.version || '';

        config.googleCloudProjectId = this.environment.GOOGLE_CLOUD_PROJECT_ID;
        if (this.fileExists(this.environment.GOOGLE_CLOUD_IDENTITY_KEY_FILE)) {
            config.gcpKeyFile = this.environment.GOOGLE_CLOUD_IDENTITY_KEY_FILE;
        }

        config.letterboxdApiKey = this.environment.LETTERBOXD_API_KEY;
        config.letterboxdApiSharedSecret = this.environment.LETTERBOXD_API_SHARED_SECRET;

        switch (this.argument) {
            case 'prod':
                config.discordBotToken = this.environment.DISCORD_PROD_BOT_TOKEN;
                config.discordClientId = this.environment.DISCORD_CLIENT_ID;
                config.firestoreCollectionId = this.environment.FIRESTORE_PROD_COLLECTION_ID;
                config.pubSubTopicName = this.environment.PUB_SUB_PROD_TOPIC_NAME;
                config.pubSubSubscriptionName = this.environment.PUB_SUB_PROD_SUBSCRIPTION_NAME;
                config.isDev = false;
                break;
            case 'dev':
                config.discordBotToken = this.environment.DISCORD_DEV_BOT_TOKEN;
                config.discordClientId = this.environment.DISCORD_DEV_CLIENT_ID;
                config.firestoreCollectionId = this.environment.FIRESTORE_DEV_COLLECTION_ID;
                config.pubSubTopicName = this.environment.PUB_SUB_DEV_TOPIC_NAME;
                config.pubSubSubscriptionName = this.environment.PUB_SUB_DEV_SUBSCRIPTION_NAME;
                config.isDev = true;
                break;
        }

        return config;
    }
}

module.exports = ConfigFactory;
