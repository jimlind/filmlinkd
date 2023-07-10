import LetterboxdTag from '../../models/letterboxd/letterboxd-tag.mjs';

export default class LetterboxdTagFactory {
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
