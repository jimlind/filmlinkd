const convict = require('convict');

var config = convict({
    live: {
        doc: 'Use live data',
        format: Boolean,
        default: 'false',
        env: 'npm_config_live',
    },
    vip: {
        doc: 'Run as a special scraper only for VIP users',
        format: Boolean,
        default: false,
        env: 'npm_config_vip',
    },
    googleCloudProjectId: {
        format: String,
        default: 'letterboxd-bot',
    },
    googleCloudIdentityKeyFile: {
        format: String,
        default: '',
    },
    discordBotTokenName: {
        format: String,
        default: '',
    },
    letterboxdApiSharedSecretName: {
        format: String,
        default: 'LETTERBOXD_API_SHARED_SECRET',
    },
    firestoreCollectionId: {
        format: String,
        default: '',
    },
    pubSub: {
        logEntry: {
            topicName: {
                format: String,
                default: '',
            },
            subscriptionName: {
                format: String,
                default: '',
            },
        },
        logEntryResult: {
            topicName: {
                format: String,
                default: '',
            },
            subscriptionName: {
                format: String,
                default: '',
            },
        },
        command: {
            topicName: {
                format: String,
                default: '',
            },
            subscriptionName: {
                format: String,
                default: '',
            },
        },
    },
});

var environment = config.get('live') ? 'production' : 'development';
config.loadFile('./config/' + environment + '.json');
config.validate({ allowed: 'strict' });

module.exports = config.getProperties();
