const LetterboxdTag = require('../../models/letterboxd/letterboxd-tag');

class LetterboxdTagFactory {
    /**
     * @param {Object} tagData
     * @returns LetterboxdTag
     */
    buildTagFromObject(tagData) {
        const letterboxdTag = new LetterboxdTag();

        letterboxdTag.tag = tagData.tag || '';
        letterboxdTag.code = tagData.code || '';
        letterboxdTag.displayTag = tagData.displayTag || '';

        return letterboxdTag;
    }
}

module.exports = LetterboxdTagFactory;
