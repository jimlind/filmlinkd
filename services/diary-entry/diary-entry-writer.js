'use strict';

class DiaryEntryWriter {
    constructor(
        discordMessageSender,
        firestorePreviousDao,
        messageEmbedFactory,
        subscribedUserList,
    ) {
        this.discordMessageSender = discordMessageSender;
        this.firestorePreviousDao = firestorePreviousDao;
        this.messageEmbedFactory = messageEmbedFactory;
        this.subscribedUserList = subscribedUserList;
    }

    write(message) {
        const { userData, diaryEntry, channel } = JSON.parse(message.data.toString());
        this.discordMessageSender.getPermissions(channel.channelId).then((permissions) => {
            const message = this.messageEmbedFactory.createDiaryEntryMessage(
                diaryEntry,
                userData,
                permissions,
            );
            this.discordMessageSender
                .send(channel.channelId, message)
                .then(() => {
                    // Work completed. Updates can be async.
                    message.ack();
                })
                .catch(() => {
                    // Do Nothing. Send failure caught and logged in MessageSender
                });
        });
    }
}

module.exports = DiaryEntryWriter;
