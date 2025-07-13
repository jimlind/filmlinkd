package jimlind.filmlinkd.system.letterboxd.model;

// https://api-docs.letterboxd.com/#/schemas/Review
public class LbReview {
  public String lbml;
  public boolean containsSpoilers;
  public boolean spoilersLocked;
  public boolean moderated;
  public String whenReviewed;
  public String text;
}
