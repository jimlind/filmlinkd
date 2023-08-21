export default class Scraper {
    /** @type {number} */
    fetchRestingTime = 15000; // 15 seconds
    /** @type {boolean} */
    fetchThreadRunning = false;
    /** @type {number} */
    fetchInterval = 0;
    /** @type {number} */
    resetRestingTime = 86400000; // 24 hours
    /** @type {number} */
    resetInterval = 0;
    /** @type {number} */
    pageSize = 30;
    /** @type {number} */
    index = 0;
    /** @type {boolean} */
    queueUserListReset = false;

    /**
     * @param {import('../dependency-injection-container.mjs')} container
     */
    constructor(container) {
        this.container = container;

        // Trigger clean up on task ending
        this.container.resolve('exitHook')(this.cleanUp.bind(this));
    }

    run() {
        const config = this.container.resolve('config');
        this.container.resolve('logger').info('Scraper Process Started', {
            name: config.get('packageName'),
            version: config.get('packageVersion'),
        });

        // Get a random index from the user list
        const getRandomIndex = this.container.resolve('subscribedUserList').getRandomIndex();
        getRandomIndex.then((randomIndex) => {
            // The recurringFetchTask uses and udates this
            this.index = randomIndex;

            const fetchTask = this.recurringFetchTask.bind(this);
            this.fetchInterval = setInterval(fetchTask, this.fetchRestingTime);

            const resetTAsk = this.recurringResetTask.bind(this);
            this.resetInterval = setInterval(resetTAsk, this.resetRestingTime);

            // Listen for Command PubSub messages posted and upsert appropriate follow outcome data
            this.container.resolve('pubSubMessageListener').onCommandMessage((message) => {
                const returnData = JSON.parse(message.data.toString());
                if (returnData.command == 'FOLLOW') {
                    message.ack();
                    const subscribedUserList = this.container.resolve('subscribedUserList');
                    subscribedUserList.upsert(returnData.user, returnData.entry);
                }
            });
        });
    }

    recurringFetchTask() {
        // This variable is reset after a page of entries processing has completed
        if (this.fetchThreadRunning) return;
        this.fetchThreadRunning = true;

        if (this.queueUserListReset) {
            this.container.resolve('subscribedUserList').cachedData = null;
            this.queueUserListReset = false;
        }

        this.container
            .resolve('diaryEntryProcessor')
            .processPageOfEntries(this.index, this.pageSize)
            .then((pageCount) => {
                this.fetchThreadRunning = false;
                this.index = pageCount === 0 ? 0 : this.index + pageCount;
            });
    }

    recurringResetTask() {
        this.queueUserListReset = true;
    }

    cleanUp(signal) {
        // Log process closure
        this.container.resolve('logger').info('Scraper Process Terminated', { signal });
        // Stop the recurring tasks
        clearInterval(this.fetchInterval);
        clearInterval(this.resetInterval);
        // Close all possible PubSub connections
        this.container.resolve('pubSubConnection').closeAllSubscriptions();
    }
}
