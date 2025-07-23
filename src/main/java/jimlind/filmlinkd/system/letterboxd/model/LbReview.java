package jimlind.filmlinkd.system.letterboxd.model;

/**
 * Implements the <a href="https://api-docs.letterboxd.com/#/schemas/Review">Review</a> schema
 * model.
 */
public class LbReview {
  public String lbml;
  public boolean containsSpoilers;
  public boolean spoilersLocked;
  public boolean moderated;
  public String whenReviewed;
  public String text;
}
