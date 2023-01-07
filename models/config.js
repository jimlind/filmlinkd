class Config {
    discordBotToken = '';
    discordClientId = '';
    firestoreCollectionId = '';
    gcpKeyFile = '';
    googleCloudProjectId = '';
    pubSubLogEntryTopicName = '';
    pubSubLogEntrySubscriptionName = '';
    letterboxdApiKey = '';
    letterboxdApiSharedSecret = '';

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
