class UserTextMessage {
    manageServer = false;
    command = '';
    /** @property {string[]} arguments - All strings passed in via command */
    arguments = [];
    /** @property {string} channelId - Message's channel Id stored as string to avoid silly rounding */
    channelId = '';
    /** @property {string} guildId - Message's guild Id stored as string to avoid silly rounding */
    guildId = '';
}

module.exports = UserTextMessage;
