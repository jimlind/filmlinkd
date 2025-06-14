package jimlind.filmlinkd.system.letterboxd.model;

import java.util.List;

// https://api-docs.letterboxd.com/#/schemas/Film
public class LBFilm {
  public String id;
  public String name;
  @FirstParty public String originalName;
  public String sortingName;
  @FirstParty public List<String> alternativeNames;
  public int releaseYear;
  public int runTime;
  public float rating;
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
  public String tagline;
  public String description;
  public LBImage backdrop;
  public float backdropFocalPoint;
  // trailer - FilmTrailer
  @FirstParty public List<LBCountry> countries;
  @Deprecated public LBLanguage originalLanguage;
  @FirstParty public LBLanguage productionLanguage;
  public LBLanguage primaryLanguage;
  @FirstParty public List<LBLanguage> languages;
  // @FirstParty releases - Release[]
  public List<LBFilmContributions> contributions;
  // news - NewsItem[]
  // recentStories - Story[]
  @FirstParty public LBFilmSummary similarTo;
  // @FirstParty themes - Theme[]
  // @FirstParty minigenres - Minigenre[]
  // @FirstParty nanogenres - Nanogenre[]
  // @FirstParty huntItems - TreasureHuntItem[]
  public List<String> targeting;
}
