import death from 'death';

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

    /**
     * @param {import('../dependency-injection-container')} container
     */
    constructor(container) {
        this.container = container;

        // Trigger clean up on task ending
        death(this.cleanUp.bind(this));
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

    cleanUp(signal, error) {
        // Log process closure
        this.container.resolve('logger').info('Scraper Process Terminated', { signal, error });
        // Stop the recurring tasks
        clearInterval(this.fetchInterval);
        clearInterval(this.resetInterval);
        // Close all possible PubSub connections
        this.container.resolve('pubSubConnection').closeAllSubscriptions();
    }
}
