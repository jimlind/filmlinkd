package jimlind.filmlinkd.system.letterboxd.model;

/**
 * Implements the <a
 * href="https://api-docs.letterboxd.com/#/schemas/FilmStatistics">FilmStatistics</a> schema model.
 */
public class LbFilmStatistics {
  // film - FilmIdentifier
  public LbFilmStatisticsCounts counts;
  public float rating;
  // ratingsHistogram - RatingsHistogramBar[]
}
