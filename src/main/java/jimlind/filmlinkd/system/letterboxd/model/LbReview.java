package jimlind.filmlinkd.system.letterboxd.model;

import lombok.Getter;

/**
 * Implements the <a href="https://api-docs.letterboxd.com/#/schemas/Review">Review</a> schema
 * model.
 */
@Getter
public class LbReview {
  public String lbml;
  public boolean containsSpoilers;
  public boolean spoilersLocked;
  public boolean moderated;
  public String whenReviewed;
  public String text;
}
