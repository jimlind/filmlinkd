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
    /** @type {number} */
    index = 0;

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
        const getRandomIndex = this.container.resolve('subscribedUserList').getRandomIndex();
        getRandomIndex.then((randomIndex) => {
            // The recurringFetchTask uses and udates this
            this.index = randomIndex;

            const fetchTask = this.recurringFetchTask.bind(this);
            this.fetchInterval = setInterval(fetchTask, this.fetchRestingTime);

            const resetTAsk = this.recurringResetTask.bind(this);
            this.resetInterval = setInterval(resetTAsk, this.resetRestingTime);
        });

        // HOPEFULLY DEPRECATED
        // Because the version of the primary bot that is running still expects this to be listening to
        // these events I need to keep it around for a bit longer to be backward compatible.
        //
        // Listen for LogEntryResult PubSub messages posted and respond
        this.container.resolve('pubSubMessageListener').onLogEntryResultMessage((message) => {
            // Acknowledge the message to remove it from the queue
            message.ack();

            // Parse the message data
            const returnData = JSON.parse(message.data.toString());

            // Write the current (most recent) message to the database
            this.container
                .resolve('firestoreUserDao')
                .getByUserName(returnData.userName)
                .then((userModel) => {
                    // Exit early if the existing diary entry id is older than the incoming diary entry id
                    if ((userModel?.previous?.id || 0) >= returnData.previousId) {
                        return;
                    }

                    this.container
                        .resolve('firestorePreviousDao')
                        .update(userModel, returnData.diaryEntry);
                });
        });
    }

    recurringFetchTask() {
        // This variable is reset after a page of entries processing has completed
        if (this.fetchThreadRunning) return;
        this.fetchThreadRunning = true;
        this.container
            .resolve('diaryEntryProcessor')
            .processPageOfEntries(this.index, this.pageSize)
            .then((pageCount) => {
                this.fetchThreadRunning = false;
                this.index = pageCount === 0 ? 0 : this.index + pageCount;
            });
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
