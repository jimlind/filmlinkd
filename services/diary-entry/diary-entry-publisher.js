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
     * @param {string[]} channelIdList
     */
    publish(diaryEntryList, channelIdList = []) {
        diaryEntryList.forEach((diaryEntry) => {
            this.pubSubConnection
                .getTopic()
                .then((topic) => {
                    const data = { entry: diaryEntry, channelIdList };
                    const buffer = Buffer.from(JSON.stringify(data));
                    topic.publish(buffer);
                })
                .catch(() => {
                    this.logger.warn(`Problem publishing a diary message`);
                });
        });
    }
}

module.exports = DiaryEntryPublisher;
