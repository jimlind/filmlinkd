const convict = require('convict');
const config = convict({
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
    packageName: {
        format: String,
        default: 'filmlinkd',
    },
    packageVersion: {
        format: String,
        default: '0',
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
    gcpKeyFile: {
        format: String,
        nullable: true,
        default: null,
    },
});

var environment = config.get('live') ? 'production' : 'development';
config.loadFile('./config/' + environment + '.json');

const packageJson = convict({}).loadFile('package.json');
const packageJsonProperties = packageJson.getProperties();

config.set('packageName', packageJsonProperties.name);
config.set('packageVersion', packageJsonProperties.version);
config.validate({ allowed: 'strict' });

module.exports = config;
