package jimlind.filmlinkd.system.letterboxd.model;

import java.util.List;
import lombok.Getter;

/**
 * Implements the <a href="https://api-docs.letterboxd.com/#/schemas/Image">Image</a> schema model.
 */
@Getter
public class LbImage {
  public List<LbImageSize> sizes;
}
