import death from 'death';

export default class Vip {
    /** @type {number} */
    fetchRestingTime = 10000; // 10 seconds
    /** @type {boolean} */
    fetchThreadRunning = false;
    /** @type {number} */
    fetchInterval = 0;
    /** @type {number} */
    resetRestingTime = 43200000; // 12 hours
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
        this.fetchInterval = setInterval(this.recurringFetchTask.bind(this), this.fetchRestingTime);
        this.resetInterval = setInterval(this.recurringResetTask.bind(this), this.resetRestingTime);
    }

    recurringFetchTask() {
        // This variable is reset after a page of entries processing has completed
        if (this.fetchThreadRunning) return;
        this.fetchThreadRunning = true;

        this.container
            .resolve('diaryEntryProcessor')
            .processPageOfVipEntries(this.index, this.pageSize)
            .then((pageCount) => {
                this.fetchThreadRunning = false;
                this.index = pageCount === 0 ? 0 : this.index + pageCount;
            });
    }

    recurringResetTask() {
        this.container.resolve('subscribedUserList').cachedVipData = null;
    }

    cleanUp(signal, error) {
        // Log process closure
        this.container.resolve('logger').info('VIP Process Terminated', { signal, error });
        // Stop the recurring tasks
        clearInterval(this.fetchInterval);
        clearInterval(this.resetInterval);
        // Close all possible PubSub connections
        this.container.resolve('pubSubConnection').closeAllSubscriptions();
    }
}