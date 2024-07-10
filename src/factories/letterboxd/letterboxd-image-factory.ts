import LetterboxdImageSize from '../../models/letterboxd/letterboxd-image-size.js';
import LetterboxdImage from '../../models/letterboxd/letterboxd-image.js';

export default class LetterboxdImageFactory {
    /**
     * @param {Object} imageData
     * @returns LetterboxdImage | null
     */
    buildImageFromObject(imageData: any) {
        if (!imageData?.sizes && !imageData?.sizes?.length) {
            return null;
        }

        const letterboxdImage = new LetterboxdImage();
        letterboxdImage.sizes = imageData.sizes.map((size: any) =>
            this.buildImageSizeFromObject(size),
        );

        return letterboxdImage;
    }

    /**
     * @param {Object} imageSizeData
     * @returns LetterboxdImageSize
     */
    buildImageSizeFromObject(imageSizeData: any) {
        const letterboxdImagesSize = new LetterboxdImageSize();

        letterboxdImagesSize.width = imageSizeData.width;
        letterboxdImagesSize.height = imageSizeData.height;
        letterboxdImagesSize.url = imageSizeData.url;

        return letterboxdImagesSize;
    }
}
