/**
 * Letterboxd API Image Size Object
 * https://api-docs.letterboxd.com/#/definitions/ImageSize
 */

export default class LetterboxdImageSize {
    /** @property {number} type The image width in pixels. */
    width = 0;
    /** @property {number} id The image height in pixels. */
    height = 0;
    /** @property {string} url The URL to the image file. */
    url = '';
}
