'use strict';

class ActionTranslator {
    constructor(
        diaryEntryPublisher,
        discordMessageSender,
        firestoreSubscriptionDao,
        firestoreUserDao,
        messageEmbedFactory,
        letterboxdProfileWeb,
        subscribedUserList,
    ) {
        this.diaryEntryPublisher = diaryEntryPublisher;
        this.discordMessageSender = discordMessageSender;
        this.firestoreSubscriptionDao = firestoreSubscriptionDao;
        this.firestoreUserDao = firestoreUserDao;
        this.messageEmbedFactory = messageEmbedFactory;
        this.letterboxdProfileWeb = letterboxdProfileWeb;
        this.subscribedUserList = subscribedUserList;
    }

    translate(message) {
        switch (message.command) {
            case 'help':
                this.help(message.channelId);
                break;
            case 'follow':
                if (message.manageServer) {
                    this.follow(message.arguments, message.channelId, message.guildId);
                } else {
                    this.discordMessageSender
                        .send(
                            message.channelId,
                            this.messageEmbedFactory.createInadequatePermissionsMessage(),
                        )
                        .catch(() => {});
                }
                break;
            case 'unfollow':
                if (message.manageServer) {
                    this.unfollow(message.arguments, message.channelId);
                } else {
                    this.discordMessageSender
                        .send(
                            message.channelId,
                            this.messageEmbedFactory.createInadequatePermissionsMessage(),
                        )
                        .catch(() => {});
                }
                break;
            case 'refresh':
                this.refresh(message.arguments, message.channelId);
                break;
            case 'following':
                this.following(message.channelId, message.guildId);
                break;
        }
    }

    help(channelId) {
        this.discordMessageSender.send(channelId, this.messageEmbedFactory.createHelpMessage());
    }

    follow(userList, channelId, guildId) {
        var subscribe = function (userData) {
            this.firestoreSubscriptionDao
                .subscribe(userData, channelId, guildId)
                .then((updatedUserData) => {
                    this.discordMessageSender
                        .send(
                            channelId,
                            this.messageEmbedFactory.createFollowSuccessMessage(updatedUserData),
                        )
                        .catch(() => {});
                    this.diaryEntryPublisher.postMostRecentEntry(updatedUserData, channelId);
                })
                .catch(() => {
                    this.discordMessageSender
                        .send(
                            channelId,
                            this.messageEmbedFactory.createDuplicateFollowMessage(userData),
                        )
                        .catch(() => {});
                });
        };

        userList.forEach((userName) => {
            //TODO: Rate limit this
            this.firestoreUserDao
                .read(userName)
                // User found so subscribe
                .then(subscribe.bind(this))
                // User not found so get the profile data then create a new user then subscribe
                .catch(() => {
                    this.letterboxdProfileWeb
                        .get(userName)
                        .then((profile) => {
                            this.firestoreUserDao
                                .create(userName, profile.name, profile.image)
                                .then(subscribe.bind(this));
                        })
                        // Failed to find a Letterboxd profile page or relavent data
                        .catch(() => {
                            this.discordMessageSender
                                .send(
                                    channelId,
                                    this.messageEmbedFactory.createNoAccountFoundMessage(userName),
                                )
                                .catch(() => {});
                        });
                });
        });
    }

    unfollow(userList, channelId) {
        userList.forEach((userName) => {
            this.firestoreSubscriptionDao
                .unsubscribe(userName, channelId)
                .then((data) => {
                    if (!data?.channelList?.length) {
                        this.subscribedUserList.remove(data.userName);
                    }
                    this.discordMessageSender
                        .send(
                            channelId,
                            this.messageEmbedFactory.createUnfollowedSuccessMessage(data),
                        )
                        .catch(() => {});
                })
                .catch(() => {
                    this.discordMessageSender
                        .send(
                            channelId,
                            this.messageEmbedFactory.createUnfollowedErrorMessage(userName),
                        )
                        .catch(() => {});
                });
        });
    }

    refresh(userList, channelId) {
        //TODO: Rate limit this
        userList.forEach((userName) => {
            this.letterboxdProfileWeb
                .get(userName)
                .then((profile) => {
                    this.firestoreUserDao
                        .update(userName, profile.name, profile.image)
                        .then((data) => {
                            this.discordMessageSender
                                .send(
                                    channelId,
                                    this.messageEmbedFactory.createRefreshSuccessMessage(data),
                                )
                                .catch(() => {});
                        })
                        .catch(() => {
                            this.discordMessageSender
                                .send(
                                    channelId,
                                    this.messageEmbedFactory.createRefreshErrorMessage(userName),
                                )
                                .catch(() => {});
                        });
                })
                .catch(() => {
                    this.discordMessageSender
                        .send(
                            channelId,
                            this.messageEmbedFactory.createRefreshErrorMessage(userName),
                        )
                        .catch(() => {});
                });
        });
    }

    following(channelId, guildId) {
        this.firestoreSubscriptionDao.list(channelId, guildId).then((userList) => {
            if (userList.length) {
                this.discordMessageSender
                    .send(channelId, this.messageEmbedFactory.createFollowingMessage(userList))
                    .catch(() => {});
            } else {
                this.discordMessageSender
                    .send(channelId, this.messageEmbedFactory.createEmptyFollowingMessage())
                    .catch(() => {});
            }
        });
    }
}

module.exports = ActionTranslator;
