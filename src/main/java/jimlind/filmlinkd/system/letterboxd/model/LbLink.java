package jimlind.filmlinkd.system.letterboxd.model;

/**
 * Implements the <a href="https://api-docs.letterboxd.com/#/schemas/Link">Link</a> schema model.
 */
public class LbLink {
  public LbLinkType type;
  public String id;
  public String url;
  public String label;
  public String checkUrl;
}
