package jimlind.filmlinkd.system.letterboxd.model;

import java.util.List;
import lombok.Getter;

/**
 * Implements the <a href="https://api-docs.letterboxd.com/#/schemas/Film">Film</a> schema model.
 */
@Getter
public class LbFilm {
  public String id;
  public String name;
  @FirstParty public String originalName;
  public String sortingName;
  @FirstParty public List<String> alternativeNames;
  public int releaseYear;
  public int runTime;
  public float rating;
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
  public String tagline;
  public String description;
  public LbImage backdrop;
  public float backdropFocalPoint;
  // trailer - FilmTrailer
  @FirstParty public List<LbCountry> countries;
  @Deprecated public LbLanguage originalLanguage;
  @FirstParty public LbLanguage productionLanguage;
  public LbLanguage primaryLanguage;
  @FirstParty public List<LbLanguage> languages;
  // @FirstParty releases - Release[]
  public List<LbFilmContributions> contributions;
  // news - NewsItem[]
  // recentStories - Story[]
  @FirstParty public LbFilmSummary similarTo;
  // @FirstParty themes - Theme[]
  // @FirstParty minigenres - Minigenre[]
  // @FirstParty nanogenres - Nanogenre[]
  // @FirstParty huntItems - TreasureHuntItem[]
  public List<String> targeting;
}
