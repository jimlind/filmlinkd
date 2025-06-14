package jimlind.filmlinkd.system.letterboxd.model;

// https://api-docs.letterboxd.com/#/schemas/FilmStatistics
public class LBFilmStatistics {
  // film - FilmIdentifier
  public LBFilmStatisticsCounts counts;
  public float rating;
  // ratingsHistogram - RatingsHistogramBar[]
}
