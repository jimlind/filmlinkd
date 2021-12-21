const LetterboxdImageSize = require('../../models/letterboxd/letterboxd-image-size');

class LetterboxdImageFactory {
    /**
     * @param {Object} linkData
     * @returns {sizes: LetterboxdImagesSize[]}
     */
    buildImageFromObject(imageData) {
        const sizeList = imageData.sizes || [];
        return { sizes: sizeList.map(this.buildImageSizeFromObject) };
    }

    /**
     * @param {Object} linkData
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
