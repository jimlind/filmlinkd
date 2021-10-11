'use strict';

class LetterboxdLikesWeb {
    root = 'https://letterboxd.com';

    /**
     * @param {import('../http-client')} httpClient
     * @param {import('htmlparser2')} htmlParser2 - Library for parsing HTML
     */
    constructor(httpClient, htmlParser2) {
        this.httpClient = httpClient;
        this.htmlParser2 = htmlParser2;
        this.domUtils = htmlParser2.DomUtils;
    }

    /**
     * @param {string} userName
     * @param {string[]} linkList
     * @param {{[url: string]: boolean}[]} initialLikedList
     * @param {number} after
     * @returns {Promise<string[]>}
     */
    get(userName, linkList, initialLikedList = [], after = 0) {
        return new Promise((resolve, reject) => {
            this.fetch(userName, after)
                .then(({ likedList, activityId, endOfActivity }) => {
                    const completeLikedList = { ...initialLikedList, ...likedList };
                    const keys = Object.keys(completeLikedList);

                    let unfoundLinks = linkList.filter((element) => {
                        return !keys.includes(element);
                    });

                    if (unfoundLinks.length == 0 || endOfActivity) {
                        const likedFilmData = Object.entries(completeLikedList);
                        const likedFilmList = likedFilmData.reduce((accumulator, current) => {
                            return current[1] ? [...accumulator, current[0]] : accumulator;
                        }, []);
                        resolve(likedFilmList);
                    } else {
                        resolve(this.get(userName, linkList, completeLikedList, activityId));
                    }
                })
                .catch(reject);
        });
    }

    fetch(userName, after) {
        const url =
            `${this.root}/ajax/activity-pagination/${userName}/` + (after ? `?after=${after}` : '');

        return this.httpClient.get(url, 10000).then((response) => {
            const dom = this.htmlParser2.parseDocument(response.data);
            const likedReviewedList = this._parseReviewedFilms(dom);
            const likedWatchedList = this._parseWatchedFilms(dom, userName);
            const likedList = { ...likedReviewedList, ...likedWatchedList };

            // Parse activity id from sections
            const section = this.domUtils.getElementsByTagName('section', dom).pop();
            const activityIdString = this.domUtils.getAttributeValue(section, 'data-activity-id');
            const activityId = parseInt(activityIdString);

            return { likedList, activityId, endOfActivity: !activityId };
        });
    }

    /**
     * @param {import("domhandler").Node | import("domhandler").Node[]} dom
     */
    _parseReviewedFilms(dom) {
        const likedList = {};

        this.domUtils.getElements({ class: 'film-detail-content' }, dom, true).forEach((detail) => {
            const h2 = this.domUtils.getElementsByTagName('h2', detail)[0];
            const a = this.domUtils.getElementsByTagName('a', h2)[0];
            const href = this.root + this.domUtils.getAttributeValue(a, 'href');
            const liked = this.domUtils.getElements(
                { class: 'has-icon icon-16 icon-liked' },
                detail,
                true,
            );

            likedList[href] = Boolean(liked.length);
        });

        return likedList;
    }

    /**
     * @param {import("domhandler").Node | import("domhandler").Node[]} dom
     * @param {any} userName
     */
    _parseWatchedFilms(dom, userName) {
        const likedList = {};

        this.domUtils
            .getElements({ tag_contains: 'a', class: 'target' }, dom, true)
            .forEach((anchor) => {
                // @ts-ignore -- TODO: Method getAttributeValue expects an Element we give a Node
                const href = this.root + this.domUtils.getAttributeValue(anchor, 'href');
                if (href.startsWith(`${this.root}/${userName}/`)) {
                    const options = { class: 'context' };
                    const context = this.domUtils.getElements(options, anchor, true);
                    likedList[href] = this.domUtils.getText(context).includes('liked');
                }
            });

        return likedList;
    }
}

module.exports = LetterboxdLikesWeb;
