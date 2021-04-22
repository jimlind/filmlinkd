class Config {
    discordBotToken = '';
    firestoreCollectionId = '';
    gcpKeyFile = '';
    googleCloudProjectId = '';
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
