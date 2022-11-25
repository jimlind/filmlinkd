'use strict';

const DiaryEntry = require('../../models/diary-entry');

/**
 * Class dealing with publishing diary entry events to Google's Pub/Sub messaging service
 */
class DiaryEntryPublisher {
    /**
     * @param {import('../letterboxd/letterboxd-lid-comparison')} letterboxdLidWeb
     * @param {import('../logger')} logger
     * @param {import('../google/pubsub/pub-sub-connection')} pubSubConnection
     */
    constructor(letterboxdLidWeb, logger, pubSubConnection) {
        this.letterboxdLidWeb = letterboxdLidWeb;
        this.logger = logger;
        this.pubSubConnection = pubSubConnection;
    }

    /**
     * @param {import('../../models/diary-entry')[]} diaryEntryList
     * @param {string} channelIdOverride
     * @return {Promise<{user:string, id: string}[]>}
     */
    publish(diaryEntryList, channelIdOverride = '') {
        return new Promise((resolve) => {
            const promiseList = diaryEntryList
                .map((diaryEntry) => {
                    return Promise.all([
                        this.pubSubConnection.getTopic(),
                        this.letterboxdLidWeb.getFromUrl(diaryEntry.link),
                    ]).then((result) => {
                        const [topic, lid] = result;
                        diaryEntry.lid = lid;

                        const data = { entry: diaryEntry, channelId: channelIdOverride };
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

    /**
     * @param {import('../../models/letterboxd/letterboxd-log-entry')[]} logEntry[]
     * @param {string} channelIdOverride
     * @return {Promise<{user:string, id: string}[]>}
     */
    publishLogEntryList(logEntryList, channelIdOverride = '') {
        return new Promise((resolve) => {
            const promiseList = logEntryList
                .map((logEntry) => {
                    return this.pubSubConnection.getTopic().then((topic) => {
                        const largestImage = (logEntry.film.poster.sizes || []).reduce(
                            (prev, current) => (current.height || 0 > prev.height ? current : prev),
                        );
                        const letterboxdLink = (logEntry.links || []).reduce((prev, current) =>
                            current.type == 'letterboxd' ? current.url : prev,
                        );
                        const publishedTimeMs = new Date(logEntry.whenCreated).getTime();
                        const watchedTimeMs = new Date(logEntry.diaryDetails?.diaryDate).getTime();

                        const diaryEntry = new DiaryEntry();
                        diaryEntry.adult = logEntry.film.adult;
                        diaryEntry.containsSpoilers = logEntry.review?.containsSpoilers || '';
                        diaryEntry.filmTitle = logEntry.film.name;
                        diaryEntry.filmYear = logEntry.film.releaseYear;
                        diaryEntry.id = logEntry.viewingId || 0;
                        diaryEntry.image = largestImage?.url || '';
                        diaryEntry.liked = logEntry.like;
                        diaryEntry.link = letterboxdLink?.url || '';
                        diaryEntry.publishedDate = publishedTimeMs;
                        diaryEntry.review = logEntry.review?.text || '';
                        diaryEntry.rewatch = logEntry.diaryDetails?.rewatch;
                        diaryEntry.starCount = logEntry.rating;
                        diaryEntry.userName = logEntry.owner.userName;
                        diaryEntry.watchedDate = watchedTimeMs;
                        diaryEntry.lid = logEntry.id;
                        diaryEntry.type = diaryEntry.review ? 'review' : 'watch';

                        const data = { entry: diaryEntry, channelId: channelIdOverride };
                        const buffer = Buffer.from(JSON.stringify(data));
                        topic.publish(buffer);

                        return {
                            userLid: logEntry.owner.id,
                            entryId: logEntry.viewingId || '',
                            entryLid: logEntry.id || '',
                        };
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
