package jimlind.filmlinkd.system.letterboxd.model;

import lombok.Getter;

/**
 * Implements the <a href="https://api-docs.letterboxd.com/#/schemas/Link">Link</a> schema model.
 */
@Getter
public class LbLink {
  public LbLinkType type;
  public String id;
  public String url;
  public String label;
  public String checkUrl;
}
