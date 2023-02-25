const convict = require('convict');

var config = convict({
    env: {
        doc: 'The application environment.',
        format: ['production', 'development'],
        default: 'development',
        env: 'NODE_ENV',
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

var env = config.get('env');
config.loadFile('./config/' + env + '.json');
config.validate({ allowed: 'strict' });

module.exports = config.getProperties();
