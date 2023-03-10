class Scraper {
    /**
     * @param {import('../dependency-injection-container')} container
     */
    constructor(container) {
        this.container = container;

        // Trigger clean up on task ending
        require('death')(this.cleanUp.bind(this));
    }

    run() {
        // Listen for LogEntryResult PubSub messages posted and respond
        this.container.resolve('pubSubMessageListener').onLogEntryResultMessage((message) => {
            // Ack the message first to remove it from the queue
            message.ack();

            const returnData = JSON.parse(message.data.toString());
            // Add new users to the cached user list or update existing data
            const upsertResult = this.container
                .resolve('subscribedUserList')
                .upsert(returnData.userName, returnData.userLid, returnData.previousId);

            // The upsertResult is the most recent id for the user.
            // Exit early if the current message isn't the most recent.
            if (upsertResult != returnData.previousId) {
                return;
            }

            // Write the current (most recent) message to the database
            this.container
                .resolve('firestoreUserDao')
                .getByUserName(returnData.userName)
                .then((userModel) =>
                    this.container.resolve('firestorePreviousDao').update(userModel, diaryEntry),
                );
        });
    }

    cleanUp() {
        // Close the PubSub connection
        this.container
            .resolve('pubSubConnection')
            .getLogEntryResultSubscription()
            .then((subscription) => subscription.close());
    }
}

module.exports = Scraper;
