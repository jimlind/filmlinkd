package jimlind.filmlinkd.system.letterboxd.utils;

import com.google.gson.Gson;
import javax.inject.Inject;
import jimlind.filmlinkd.system.letterboxd.model.LbImage;
import jimlind.filmlinkd.system.letterboxd.model.LbImageSize;

/** Utilities to translate Letterboxd Image objects to other formats. */
public class ImageUtils {
  /** Constructor for this class. */
  @Inject
  public ImageUtils() {}

  /**
   * Get the URL of the tallest image from a Letterboxd image model.
   *
   * @param image A Letterboxd image model, this represents multiple images.
   * @return The URL for the tallest image or a blank string if input isn't compatible
   */
  public String getTallest(LbImage image) {
    if (image == null || image.sizes == null) {
      return "";
    }

    String json = "{ \"url\": \"\", \"height\": 0, \"width\": 0 }";
    LbImageSize emptyImage = new Gson().fromJson(json, LbImageSize.class);
    LbImageSize tallestImage =
        image.getSizes().stream()
            .reduce(
                emptyImage,
                (result, next) -> next.getHeight() > result.getHeight() ? next : result);

    return tallestImage.url;
  }
}
