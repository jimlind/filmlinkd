package jimlind.filmlinkd.system.letterboxd.model;

import lombok.Getter;

/**
 * Implements the <a href="https://api-docs.letterboxd.com/#/schemas/ImageSize">ImageSize</a> schema
 * model.
 */
@Getter
public class LbImageSize {
  public int width;
  public int height;
  public String url;
}
