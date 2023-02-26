class BotVip {
    /** @type {number} */
    botRestingTime = 10000; // 10 seconds
    /** @type {boolean} */
    threadRunning = false;
    /** @type {number} */
    interval = 0;
    /** @type {number} */
    currentPage = 0;

    /**
     * @param {import('../dependency-injection-container')} container
     */
    constructor(container) {
        this.container = container;

        // Trigger clean up on task ending
        require('death')(this.cleanUp.bind(this));
    }

    run() {
        this.interval = setInterval(this.recurringTask.bind(this), this.botRestingTime);
    }

    recurringTask() {
        // This variable is reset after a page of entries processing has completed
        if (this.threadRunning) return;
        this.threadRunning = true;

        this.container
            .resolve('diaryEntryProcessor')
            .processPageOfVipEntries(this.currentPage, 30)
            .then((pageCount) => {
                this.threadRunning = false;
                this.currentPage = pageCount === 0 ? 0 : this.currentPage + pageCount;
            });
    }

    cleanUp(signal, error) {
        // Stop the recurring task
        clearInterval(this.interval);
        // Close the subscription
        this.container
            .resolve('pubSubConnection')
            .getLogEntrySubscription()
            .then((subscription) => {
                subscription.close();
            });
        // Log termination
        this.container.resolve('logger').info('Program Terminated', { signal, error });
    }
}

module.exports = BotVip;
