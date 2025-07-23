package jimlind.filmlinkd.system.letterboxd.model;

import java.util.List;

/**
 * Implements the <a href="https://api-docs.letterboxd.com/#/schemas/FilmSummary">FilmSummary</a>
 * schema model.
 */
public class LbFilmSummary {
  public String id;
  public String name;
  public String originalName;
  public String sortingName;
  public List<String> alternativeNames;
  public int releaseYear;
  public int runTime;
  public float rating;
  public List<LbContributorSummary> directors;
  public LbImage poster;
  public LbImage adultPoster;
  public int top250Position;
  public boolean adult;
  public boolean reviewsHidden;
  public boolean posterCustomisable;
  public String filmCollectionId;
  public List<LbLink> links;
  // relationships - MemberFilmRelationship[]
  public List<LbGenre> genres;
}
