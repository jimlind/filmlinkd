/**
 * Class dealing with publishing diary entry events to Google's Pub/Sub messaging service
 */
export default class DiaryEntryPublisher {
    /**
     * @param {import('../../factories/diary-entry-factory.mjs')} diaryEntryFactory
     * @param {import('../letterboxd/letterboxd-lid-comparison.mjs')} letterboxdLidWeb
     * @param {import('../logger.mjs')} logger
     * @param {import('../google/pubsub/pub-sub-connection.mjs')} pubSubConnection
     */
    constructor(diaryEntryFactory, letterboxdLidWeb, logger, pubSubConnection) {
        this.diaryEntryFactory = diaryEntryFactory;
        this.letterboxdLidWeb = letterboxdLidWeb;
        this.logger = logger;
        this.pubSubConnection = pubSubConnection;
    }

    /**
     * Publish a list of Diary Entries (Log Entries) in the appropriate Pub/Sub
     * Attaches a LID to the Entry for forward compatibility
     *
     * @param {import('../../models/diary-entry.mjs')[]} diaryEntryList
     * @param {string} channelIdOverride
     * @return {Promise}
     */
    publish(diaryEntryList, channelIdOverride = '') {
        const promiseList = diaryEntryList.map((diaryEntry) => {
            return Promise.all([
                this.pubSubConnection.getLogEntryTopic(),
                this.letterboxdLidWeb.getFromUrl(diaryEntry.link),
            ]).then(([topic, lid]) => {
                diaryEntry.lid = lid;

                const data = { entry: diaryEntry, channelId: channelIdOverride };
                const buffer = Buffer.from(JSON.stringify(data));
                topic.publishMessage({ data: buffer });
            });
        });

        return Promise.all(promiseList);
    }

    /**
     * @param {import('../../models/letterboxd/letterboxd-log-entry.mjs')[]} logEntry[]
     * @param {string} channelIdOverride
     * @param {string} source
     * @return {Promise<{user:string, id: string}[]>}
     */
    publishLogEntryList(logEntryList, channelIdOverride = '', source = 'Normal') {
        return new Promise((resolve) => {
            const promiseList = logEntryList
                .map((logEntry) => {
                    return this.pubSubConnection.getLogEntryTopic().then((topic) => {
                        const letterboxdLink = (logEntry.links || []).reduce((prev, current) =>
                            current.type == 'letterboxd' ? current.url : prev,
                        );
                        const publishedTimeMs = new Date(logEntry.whenCreated).getTime();
                        const updatedTimeMs = new Date(logEntry.whenUpdated).getTime();
                        const watchedTimeMs = new Date(logEntry.diaryDetails?.diaryDate).getTime();

                        const diaryEntry = this.diaryEntryFactory.create();
                        diaryEntry.adult = logEntry.film.adult;
                        diaryEntry.containsSpoilers = logEntry.review?.containsSpoilers || '';
                        diaryEntry.filmTitle = logEntry.film.name;
                        diaryEntry.filmYear = logEntry.film.releaseYear;
                        diaryEntry.id = logEntry.viewingId || 0;
                        diaryEntry.image = logEntry.film?.poster?.getLargestImage() || '';
                        diaryEntry.liked = logEntry.like;
                        diaryEntry.link = letterboxdLink?.url || '';
                        diaryEntry.publishedDate = publishedTimeMs;
                        diaryEntry.review = logEntry.review?.text || '';
                        diaryEntry.rewatch = logEntry.diaryDetails?.rewatch;
                        diaryEntry.starCount = logEntry.rating;
                        diaryEntry.userName = logEntry.owner.userName;
                        diaryEntry.userLid = logEntry.owner.id;
                        diaryEntry.watchedDate = watchedTimeMs;
                        diaryEntry.lid = logEntry.id;
                        diaryEntry.type = diaryEntry.review ? 'review' : 'watch';

                        // Used to create logging metrics for publish delay
                        diaryEntry.updatedDate = updatedTimeMs;
                        diaryEntry.publishSource = source;

                        const data = { entry: diaryEntry, channelId: channelIdOverride };
                        const buffer = Buffer.from(JSON.stringify(data));
                        topic.publishMessage({ data: buffer });

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
