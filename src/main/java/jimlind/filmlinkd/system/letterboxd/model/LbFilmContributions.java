package jimlind.filmlinkd.system.letterboxd.model;

import java.util.List;

/**
 * Implements the <a
 * href="https://api-docs.letterboxd.com/#/schemas/FilmContributions">FilmContributions</a> schema
 * model.
 */
public class LbFilmContributions {
  public LbContributionType type;
  public List<LbContributorSummary> contributors;
}
