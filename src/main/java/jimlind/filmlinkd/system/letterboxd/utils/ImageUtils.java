package jimlind.filmlinkd.system.letterboxd.utils;

import jimlind.filmlinkd.system.letterboxd.model.LbImage;
import jimlind.filmlinkd.system.letterboxd.model.LbImageSize;

public class ImageUtils {
  public String getTallest(LbImage image) {
    if (image == null || image.sizes == null) {
      return "";
    }

    LbImageSize emptyImage = new LbImageSize();
    emptyImage.url = "";
    emptyImage.height = 0;
    emptyImage.width = 0;

    LbImageSize tallestImage =
        image.sizes.stream()
            .reduce(emptyImage, (result, next) -> next.height > result.height ? next : result);

    return tallestImage.url;
  }
}
