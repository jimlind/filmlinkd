'use strict';

const DiaryEntry = require('../../models/diary-entry');

class LetterboxdDiaryRss {
    /**
     * @param {import('axios').default} axios - Library for downloading
     * @param {import('htmlparser2')} htmlParser2 - Library for parsing HTML
     */
    constructor(axios, htmlParser2) {
        this.axios = axios;
        this.htmlParser2 = htmlParser2;
    }

    /**
     * @param {string} userName
     * @param {number} count
     * @returns {Promise<DiaryEntry[]>}
     */
    get(userName, count) {
        return new Promise((resolve, reject) => {
            this.axios
                .get(`https://letterboxd.com/${userName}/rss/`, {
                    headers: {
                        'User-Agent': 'Filmlinkd - A Letterboxd Discord Bot',
                    },
                })
                .then((response) => {
                    resolve(this.parseRss(userName, response.data, count));
                })
                .catch(reject);
        });
    }

    /**
     * @param {string} userName
     * @param {string} responseText
     * @param {number} count
     * @returns {DiaryEntry[]}
     */
    parseRss(userName, responseText, count) {
        const dom = this.htmlParser2.parseDocument(responseText, { xmlMode: true });
        const itemList = this.htmlParser2.DomUtils.getElementsByTagName('item', dom);

        const entryList = [];
        for (let i = 0; i < itemList.length; i++) {
            // If it doesn't have a film title then we don't care about it. Ignore it.
            const filmTitle = this.getFilmTitle(itemList[i]);
            if (!filmTitle) continue;

            entryList.push(this.createEntry(userName, itemList[i]));

            // Once we hit the number we want, stop processing data
            if (entryList.length >= count) {
                break;
            }
        }

        return entryList;
    }

    /**
     * @param {string} userName
     * @param {import("domhandler").Element} item
     * @returns {DiaryEntry}
     */
    createEntry(userName, item) {
        const description = this.getDescriptionDom(item);

        const diaryEntry = new DiaryEntry();
        diaryEntry.id = this.getId(item);
        diaryEntry.userName = userName;
        diaryEntry.type = this.getType(item);
        diaryEntry.link = this.getLink(item);
        diaryEntry.publishedDate = this.getPublishedDate(item);
        diaryEntry.filmTitle = this.getFilmTitle(item);
        diaryEntry.filmYear = this.getFilmYear(item);
        diaryEntry.watchedDate = this.getWatchedDate(item);
        diaryEntry.image = this.getImage(description);
        diaryEntry.starCount = this.getStarCount(item);
        diaryEntry.stars = this.getStars(item);
        diaryEntry.rewatch = this.getRewatch(item);
        diaryEntry.containsSpoilers = this.getContainsSpoilers(item);
        diaryEntry.review = this.getReview(description);

        return diaryEntry;
    }

    /**
     * @param {import("domhandler").Element} item
     * @returns {import("domhandler").Document}
     */
    getDescriptionDom(item) {
        const descriptionHtml = this.getTextFromTag('description', item);
        return this.htmlParser2.parseDocument(descriptionHtml, { xmlMode: true });
    }

    /**
     * @param {import("domhandler").Element} item
     * @returns {number}
     */
    getId(item) {
        const results = this.getTextFromTag('guid', item).match(/letterboxd-.*-(\d+)/);
        return parseInt(results[1]);
    }

    /**
     * @param {import("domhandler").Element} item
     * @returns {string}
     */
    getType(item) {
        const results = this.getTextFromTag('guid', item).match(/letterboxd-(.*)-\d+/);
        return results[1];
    }

    /**
     * @param {import("domhandler").Element} item
     * @returns {string}
     */
    getLink(item) {
        return this.getTextFromTag('link', item);
    }

    /**
     * @param {import("domhandler").Element} item
     * @returns {number}
     */
    getPublishedDate(item) {
        const pubDateString = this.getTextFromTag('pubDate', item);
        return new Date(pubDateString).getTime();
    }

    /**
     * @param {import("domhandler").Element} item
     * @returns {number}
     */
    getWatchedDate(item) {
        const watchedDateString = this.getTextFromTag('letterboxd:watchedDate', item);
        if (!watchedDateString) {
            return 0;
        }

        return new Date(watchedDateString).getTime();
    }

    /**
     * @param {import("domhandler").Element} item
     * @returns {string}
     */
    getFilmTitle(item) {
        return this.getTextFromTag('letterboxd:filmTitle', item);
    }

    /**
     * @param {import("domhandler").Element} item
     * @returns {number}
     */
    getFilmYear(item) {
        return parseInt(this.getTextFromTag('letterboxd:filmYear', item));
    }

    /**
     * @param {import("domhandler").Document} item
     * @returns {string}
     */
    getImage(item) {
        const elements = this.htmlParser2.DomUtils.getElementsByTagName('img', item);
        if (!elements.length) {
            return '';
        }
        return this.htmlParser2.DomUtils.getAttributeValue(elements[0], 'src') || '';
    }

    /**
     * @param {import("domhandler").Element} item
     * @returns {number}
     */
    getStarCount(item) {
        return parseFloat(this.getTextFromTag('letterboxd:memberRating', item)) || 0;
    }

    /**
     * @param {import("domhandler").Element} item
     * @returns {string}
     */
    getStars(item) {
        const rating = this.getTextFromTag('letterboxd:memberRating', item);
        const map = {
            0.5: '½',
            '1.0': ':star:',
            1.5: ':star:½',
            '2.0': ':star::star:',
            2.5: ':star::star:½',
            '3.0': ':star::star::star:',
            3.5: ':star::star::star:½',
            '4.0': ':star::star::star::star:',
            4.5: ':star::star::star::star:½',
            '5.0': ':star::star::star::star::star:',
        };
        return map[rating] || '';
    }

    /**
     * @param {import("domhandler").Element} item
     * @returns {boolean}
     */
    getRewatch(item) {
        return this.getTextFromTag('letterboxd:rewatch', item) === 'Yes';
    }

    /**
     * @param {import("domhandler").Document} item
     * @returns {string}
     */
    getReview(item) {
        const paragraphs = this.htmlParser2.DomUtils.getElementsByTagName('p', item);
        return paragraphs
            .reduce((accumulator, currentValue) => {
                const paragraphText = this.htmlParser2.DomUtils.getText(currentValue).trim();
                // Don't add an autogenerated paragraphs that say there are spoilers
                if (paragraphText == 'This review may contain spoilers.') {
                    return accumulator;
                }
                // Don't add any autogenerated paragraphs that say when it was watched
                const regex = /^Watched on .+day \w+ \d+, \d{4}\.$/g;
                if (paragraphText.match(regex)) {
                    return accumulator;
                }

                return accumulator + '\n' + paragraphText;
            }, '')
            .trim();
    }

    /**
     * @param {import("domhandler").Element} item
     * @returns {boolean}
     */
    getContainsSpoilers(item) {
        return this.getTextFromTag('title', item).includes('(contains spoilers)');
    }

    /**
     * @param {string | ((name: string) => boolean)} tag
     * @param {import("domhandler").Element} item
     * @returns {string}
     */
    getTextFromTag(tag, item) {
        const elements = this.htmlParser2.DomUtils.getElementsByTagName(tag, item);
        if (!elements.length) {
            return '';
        }

        return this.htmlParser2.DomUtils.getText(elements[0]);
    }
}

module.exports = LetterboxdDiaryRss;
