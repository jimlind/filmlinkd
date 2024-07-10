import LetterboxdLink from '../../models/letterboxd/letterboxd-link.js';

export default class LetterboxdLinkFactory {
    /**
     * @param {Object} linkData
     * @returns LetterboxdLink
     */
    buildLinkFromObject(linkData: any) {
        const letterboxdLink = new LetterboxdLink();

        letterboxdLink.type = linkData.type;
        letterboxdLink.id = linkData.id;
        letterboxdLink.url = linkData.url;

        return letterboxdLink;
    }
}
