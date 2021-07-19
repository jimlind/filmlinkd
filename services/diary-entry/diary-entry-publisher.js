'use strict';

class DiaryEntryPublisher {
    /**
     * @param {import('../google/pubsub/pub-sub-connection')} pubSubConnection
     * @param {import('../logger')} logger
     */
    constructor(pubSubConnection, logger) {
        this.pubSubConnection = pubSubConnection;
        this.logger = logger;
    }

    /**
     * @param {import('../../models/diary-entry')[]} diaryEntryList
     */
    publish(diaryEntryList) {
        diaryEntryList.forEach((diaryEntry) => {
            this.pubSubConnection
                .getTopic()
                .then((topic) => {
                    const buffer = Buffer.from(JSON.stringify(diaryEntry));
                    topic.publish(buffer);
                })
                .catch(() => {
                    this.logger.warn(`Problem publishing a diary message`);
                });
        });
    }
}

module.exports = DiaryEntryPublisher;
