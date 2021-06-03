'use strict';

const UserTextMessage = require('../../models/user-text-message');

class DiscordMessageListener {
    constructor(discordConnection) {
        this.discordConnection = discordConnection;
    }

    onMessage(callback) {
        // Get connected client and listen for messages
        this.discordConnection.getConnectedClient().then((client) => {
            client.on('message', (message) => {
                // Only process actions that start with a '!`
                if (message.content[0] === '!') {
                    callback(this.createUserTextMessage(message));
                }
            });
        });
    }

    createUserTextMessage(message) {
        // Convert to plain lower case text split on the first space
        const messageTextList = message.content.toLowerCase().split(/ +/);

        // Callback on the message parsed into a subscription message object
        const userTextMessage = new UserTextMessage();

        userTextMessage.manageServer = message.member?.permissions?.has('MANAGE_GUILD') || false;
        userTextMessage.command = messageTextList[0].substring(1);
        userTextMessage.channelId = message.channel.id;
        userTextMessage.guildId = message.guild.id;
        userTextMessage.arguments = messageTextList.slice(1);

        return userTextMessage;
    }
}

module.exports = DiscordMessageListener;
