package jimlind.filmlinkd.system.letterboxd.utils;

import jimlind.filmlinkd.system.letterboxd.model.LBImage;
import jimlind.filmlinkd.system.letterboxd.model.LBImageSize;

public class ImageUtils {
  public String getTallest(LBImage image) {
    if (image == null || image.sizes == null) {
      return "";
    }

    LBImageSize emptyImage = new LBImageSize();
    emptyImage.url = "";
    emptyImage.height = 0;
    emptyImage.width = 0;

    LBImageSize tallestImage =
        image.sizes.stream()
            .reduce(emptyImage, (result, next) -> next.height > result.height ? next : result);

    return tallestImage.url;
  }
}
