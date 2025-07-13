package jimlind.filmlinkd.system.letterboxd.model;

// https://api-docs.letterboxd.com/#/schemas/FilmStatistics
public class LbFilmStatistics {
  // film - FilmIdentifier
  public LbFilmStatisticsCounts counts;
  public float rating;
  // ratingsHistogram - RatingsHistogramBar[]
}
