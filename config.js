const convict = require('convict');
const config = convict({
    live: {
        doc: 'Use live data',
        format: Boolean,
        default: false,
        env: 'npm_config_live',
    },
    mode: {
        doc: 'How we should be running the bot',
        format: ['solo', 'sharded', 'vip'],
        default: 'solo',
        env: 'npm_config_mode',
    },
    port: {
        doc: 'The port to bind.',
        format: 'port',
        default: 8080,
        env: 'PORT',
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
    letterboxdApiKeyName: {
        format: String,
        default: 'LETTERBOXD_API_KEY',
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
