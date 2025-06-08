package jimlind.filmlinkd.system.letterboxd.model;

import java.util.List;

// https://api-docs.letterboxd.com/#/schemas/FilmSummary
public class LBFilmSummary {
  public String id;
  public String name;
  public String originalName;
  public String sortingName;
  public List<String> alternativeNames;
  public int releaseYear;
  public int runTime;
  public float rating;
  public List<LBContributorSummary> directors;
  public LBImage poster;
  public LBImage adultPoster;
  public int top250Position;
  public boolean adult;
  public boolean reviewsHidden;
  public boolean posterCustomisable;
  public String filmCollectionId;
  public List<LBLink> links;
  // relationships - MemberFilmRelationship[]
  public List<LBGenre> genres;
}
