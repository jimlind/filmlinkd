const LetterboxdImage = require('../../models/letterboxd/letterboxd-image');
const LetterboxdImageSize = require('../../models/letterboxd/letterboxd-image-size');

class LetterboxdImageFactory {
    /**
     * @param {Object} imageData
     * @returns LetterboxdImage | null
     */
    buildImageFromObject(imageData) {
        if (!imageData?.sizes && !imageData?.sizes?.length) {
            return null;
        }

        const letterboxdImage = new LetterboxdImage();
        letterboxdImage.sizes = imageData.sizes.map((size) => this.buildImageSizeFromObject(size));

        return letterboxdImage;
    }

    /**
     * @param {Object} imageSizeData
     * @returns LetterboxdImageSize
     */
    buildImageSizeFromObject(imageSizeData) {
        const letterboxdImagesSize = new LetterboxdImageSize();

        letterboxdImagesSize.width = imageSizeData.width;
        letterboxdImagesSize.height = imageSizeData.height;
        letterboxdImagesSize.url = imageSizeData.url;

        return letterboxdImagesSize;
    }
}

module.exports = LetterboxdImageFactory;
