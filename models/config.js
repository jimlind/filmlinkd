class Config {
    discordBotTokenName = '';
    discordClientId = '';
    firestoreCollectionId = '';
    gcpKeyFile = '';
    googleCloudProjectId = '';

    pubSubLogEntryTopicName = '';
    pubSubLogEntrySubscriptionName = '';
    pubSubLogEntryResultTopicName = '';
    pubSubLogEntryResultSubscriptionName = '';
    pubSubCommandTopicName = '';
    pubSubCommandSubscriptionName = '';

    letterboxdApiKey = '';
    letterboxdApiSharedSecretName = '';

    isDev = false;
    isVIP = false;

    packageName = '';
    packageVersion = '';

    rssDelay = 1000;
    pageSize = 60;

    getIsDev() {
        return this.isDev;
    }
}

module.exports = Config;
