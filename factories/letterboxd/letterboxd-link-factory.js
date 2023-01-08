const LetterboxdLink = require('../../models/letterboxd/letterboxd-link');

class LetterboxdLinkFactory {
    /**
     * @param {Object} linkData
     * @returns LetterboxdLink
     */
    buildLinkFromObject(linkData) {
        const letterboxdLink = new LetterboxdLink();

        letterboxdLink.type = linkData.type;
        letterboxdLink.id = linkData.id;
        letterboxdLink.url = linkData.url;

        return letterboxdLink;
    }
}

module.exports = LetterboxdLinkFactory;
