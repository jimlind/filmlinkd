/**
 * Letterboxd API Image Object
 * https://api-docs.letterboxd.com/#/definitions/Image
 */

export default class LetterboxdImage {
    /** @property {import('./letterboxd-image-size')[]} sizes The available sizes for the image. */
    sizes = [];

    /**
     * @param {import('../models/letterboxd/letterboxd-image-size.mjs')[]} sizes
     * return string
     */
    getSmallestImage() {
        const findSmallest = (previous, current) =>
            current.height < (previous.height || Infinity) ? current : previous;
        const smallestImage = this.sizes.reduce(findSmallest, {});
        return smallestImage?.url || '';
    }

    /**
     * @param {import('../models/letterboxd/letterboxd-image-size.mjs')[]} sizes
     * return string
     */
    getLargestImage() {
        const findLargest = (previous, current) =>
            current.height > (previous.height || 0) ? current : previous;
        const largestImage = this.sizes.reduce(findLargest, {});
        return largestImage?.url || '';
    }
}
