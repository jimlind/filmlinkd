class Scraper {
    /** @type {number} */
    botRestingTime = 30000; // 30 seconds
    /** @type {boolean} */
    threadRunning = false;
    /** @type {number} */
    interval = 0;
    /** @type {number} */
    pageSize = 60;

    /**
     * @param {import('../dependency-injection-container')} container
     */
    constructor(container) {
        this.container = container;

        // Trigger clean up on task ending
        require('death')(this.cleanUp.bind(this));
    }

    run() {
        // Get a random index from the user list
        this.container
            .resolve('subscribedUserList')
            .getRandomIndex()
            .then((index) => {
                this.interval = setInterval(() => {
                    if (this.threadRunning) return;
                    this.threadRunning = true;

                    this.container
                        .resolve('diaryEntryProcessor')
                        .processPageOfEntries(index, 60)
                        .then((pageCount) => {
                            this.threadRunning = false;
                            index = pageCount === 0 ? 0 : index + pageCount;
                        });
                }, this.botRestingTime);
            });

        // Listen for LogEntryResult PubSub messages posted and respond
        this.container.resolve('pubSubMessageListener').onLogEntryResultMessage((message) => {
            // Parse the message data
            const returnData = JSON.parse(message.data.toString());

            // Add new users to the cached user list or update existing data
            // The upsertResult is the most recent id for the user.
            const upsertResult = this.container
                .resolve('subscribedUserList')
                .upsert(returnData.userName, returnData.userLid, returnData.previousId);

            // Exit early if the current message isn't the most recent.
            if (upsertResult != returnData.previousId) {
                message.ack();
                return;
            }

            // Write the current (most recent) message to the database
            this.container
                .resolve('firestoreUserDao')
                .getByUserName(returnData.userName)
                .then((userModel) => {
                    message.ack();
                    this.container
                        .resolve('firestorePreviousDao')
                        .update(userModel, returnData.diaryEntry);
                });
        });
    }

    cleanUp() {
        // Stop the recurring task
        clearInterval(this.interval);
        // Close the PubSub connection
        this.container
            .resolve('pubSubConnection')
            .getLogEntryResultSubscription()
            .then((subscription) => subscription.close());
    }
}

module.exports = Scraper;
