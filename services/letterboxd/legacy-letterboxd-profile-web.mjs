export default class LegacyLetterboxdProfileWeb {
    /**
     * @param {import('../http-client')} httpClient
     * @param {import('htmlparser2')} htmlParser2 - Library for parsing HTML
     */
    constructor(httpClient, htmlParser2) {
        this.httpClient = httpClient;
        this.htmlParser2 = htmlParser2;
        this.domUtils = htmlParser2.DomUtils;
    }

    get(userName) {
        return new Promise((resolve, reject) => {
            this.httpClient
                .get(`https://letterboxd.com/${userName}/`, 10000)
                .then((response) => {
                    const dom = this.htmlParser2.parseDocument(response.data);
                    const avatar = this.domUtils.getElements(
                        // Use `tag_contains` here so tag and class are both checked against
                        { tag_contains: 'div', class: 'profile-avatar' },
                        dom,
                    );
                    if (!avatar.length) {
                        reject('Avatar not found'); //TODO: A constant
                    }

                    const img = this.domUtils.getElementsByTagName('img', avatar, true, 1);
                    if (!img.length) {
                        reject('Image not found'); //TODO: A constant
                    }

                    resolve({
                        name: this.domUtils.getAttributeValue(img[0], 'alt'),
                        image: this.domUtils.getAttributeValue(img[0], 'src'),
                    });
                })
                .catch(reject);
        });
    }
}
