'use strict';

class DiaryEntryPublisher {
    /**
     * @param {*} logger
     * @param {import('../google/pubsub/pub-sub-connection')} pubSubConnection
     */
    constructor(logger, pubSubConnection) {
        this.logger = logger;
        this.pubSubConnection = pubSubConnection;
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
