'use strict';

class LetterboxdLikesWeb {
    constructor(axios, htmlParser2) {
        this.axios = axios;
        this.htmlParser2 = htmlParser2;
        this.domUtils = htmlParser2.DomUtils;
    }

    get(userName, count) {
        return new Promise((resolve, reject) => {
            this.axios
                .get(`https://letterboxd.com/${userName}/likes/films/`)
                .then((response) => {
                    const dom = this.htmlParser2.parseDocument(response.data);
                    const posterList = this.domUtils.getElements(
                        // Use `tag_contains` here so tag and class are both checked against
                        { tag_contains: 'div', class: 'poster film-poster really-lazy-load' },
                        dom,
                        true,
                        count,
                    );

                    const filmSlugList = [];
                    posterList.forEach((poster) => {
                        const filmSlug = this.domUtils.getAttributeValue(poster, 'data-film-slug');
                        filmSlugList.push(filmSlug);
                    });

                    resolve(filmSlugList);
                })
                .catch(reject);
        });
    }
}

module.exports = LetterboxdLikesWeb;
