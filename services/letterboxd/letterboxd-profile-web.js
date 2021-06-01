'use strict';

const axios = require('axios').default;
const htmlparser2 = require('htmlparser2');

class LetterboxdProfileWeb {
    get(userName) {
        return new Promise((resolve, reject) => {
            axios
                .get(`https://letterboxd.com/${userName}/`)
                .then((response) => {
                    const dom = htmlparser2.parseDocument(response.data);

                    const avatar = htmlparser2.DomUtils.getElements(
                        // Use `tag_contains` here so tag and class are both checked against
                        { tag_contains: 'div', class: 'profile-avatar' },
                        dom,
                    );
                    if (!avatar.length) {
                        reject('Avatar not found'); //TODO: A constant
                    }

                    const img = htmlparser2.DomUtils.getElementsByTagName('img', avatar, true, 1);
                    if (!img.length) {
                        reject('Image not found'); //TODO: A constant
                    }

                    resolve({
                        name: htmlparser2.DomUtils.getAttributeValue(img[0], 'alt'),
                        image: htmlparser2.DomUtils.getAttributeValue(img[0], 'src'),
                    });
                })
                .catch(reject);
        });
    }
}

module.exports = LetterboxdProfileWeb;
