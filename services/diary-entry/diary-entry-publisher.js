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
     * @return {Promise<{user:string, id: string}[]>}
     */
    publish(diaryEntryList, channelIdList = []) {
        return new Promise((resolve) => {
            const promiseList = diaryEntryList
                .map((diaryEntry) => {
                    return this.pubSubConnection.getTopic().then((topic) => {
                        const data = { entry: diaryEntry, channelIdList };
                        const buffer = Buffer.from(JSON.stringify(data));
                        topic.publish(buffer);

                        return { user: diaryEntry.userName, id: diaryEntry.id };
                    });
                })
                .map((p) => p.catch(() => null));

            Promise.all(promiseList).then((promiseResult) => {
                resolve(promiseResult.filter(Boolean));
            });
        });
    }
}

module.exports = DiaryEntryPublisher;
