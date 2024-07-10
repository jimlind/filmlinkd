import LetterboxdTag from '../../models/letterboxd/letterboxd-tag.js';

export default class LetterboxdTagFactory {
    /**
     * @param {Object} tagData
     * @returns LetterboxdTag
     */
    buildTagFromObject(tagData: any) {
        const letterboxdTag = new LetterboxdTag();

        letterboxdTag.tag = tagData.tag || '';
        letterboxdTag.code = tagData.code || '';
        letterboxdTag.displayTag = tagData.displayTag || '';

        return letterboxdTag;
    }
}
