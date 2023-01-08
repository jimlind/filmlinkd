/**
 * Letterboxd API Image Object
 * https://api-docs.letterboxd.com/#/definitions/Image
 */

class LetterboxdImage {
    /** @property {import('./letterboxd-image-size')[]} sizes The available sizes for the image. */
    sizes = [];
}

module.exports = LetterboxdImage;
