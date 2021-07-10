'use strict';

class LetterboxdLikesWeb {
    root = 'https://letterboxd.com';

    constructor(axios, htmlParser2) {
        this.axios = axios;
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

        return new Promise((resolve, reject) => {
            this.axios
                .get(url)
                .then((response) => {
                    const likedList = {};
                    const dom = this.htmlParser2.parseDocument(response.data);

                    // Parse reviewed films from activity
                    this.domUtils
                        .getElements({ class: 'film-detail-content' }, dom)
                        .forEach((detail) => {
                            const h2 = this.domUtils.getElementsByTagName('h2', detail)[0];
                            const a = this.domUtils.getElementsByTagName('a', h2)[0];
                            const href = this.root + this.domUtils.getAttributeValue(a, 'href');
                            const liked = this.domUtils.getElements(
                                { class: 'has-icon icon-16 icon-liked' },
                                detail,
                            );

                            likedList[href] = Boolean(liked.length);
                        });

                    // Parse watched films from activity
                    this.domUtils
                        .getElements({ tag_contains: 'a', class: 'target' }, dom)
                        .forEach((anchor) => {
                            const href =
                                this.root + this.domUtils.getAttributeValue(anchor, 'href');
                            if (href.startsWith(`${this.root}/${userName}/`)) {
                                const options = { class: 'context' };
                                const context = this.domUtils.getElements(options, anchor);
                                likedList[href] = this.domUtils.getText(context).includes('liked');
                            }
                        });

                    // Parse activity id from sections
                    const section = this.domUtils.getElementsByTagName('section', dom).pop();
                    const activityId = this.domUtils.getAttributeValue(section, 'data-activity-id');

                    resolve({ likedList, activityId, endOfActivity: !activityId });
                })
                .catch(reject);
        });
    }
}

module.exports = LetterboxdLikesWeb;
