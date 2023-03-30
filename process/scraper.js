class Scraper {
    /** @type {number} */
    fetchRestingTime = 30000; // 30 seconds
    /** @type {boolean} */
    fetchThreadRunning = false;
    /** @type {number} */
    fetchInterval = 0;
    /** @type {number} */
    resetRestingTime = 86400000; // 24 hours
    /** @type {number} */
    resetInterval = 0;
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
                this.fetchInterval = setInterval(() => {
                    if (this.fetchThreadRunning) return;
                    this.fetchThreadRunning = true;

                    this.container
                        .resolve('diaryEntryProcessor')
                        .processPageOfEntries(index, 60)
                        .then((pageCount) => {
                            this.fetchThreadRunning = false;
                            index = pageCount === 0 ? 0 : index + pageCount;
                        });
                }, this.fetchRestingTime);
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

        this.resetInterval = setInterval(this.recurringResetTask.bind(this), this.resetRestingTime);
    }

    recurringResetTask() {
        this.container.resolve('subscribedUserList').cachedData = [];
    }

    cleanUp() {
        // Stop the recurring tasks
        clearInterval(this.fetchInterval);
        clearInterval(this.resetInterval);
        // Close the PubSub connection
        this.container.resolve('pubSubConnection').closeLogEntryResultSubscription();
    }
}

module.exports = Scraper;
