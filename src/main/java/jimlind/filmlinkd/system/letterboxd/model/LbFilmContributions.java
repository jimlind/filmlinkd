package jimlind.filmlinkd.system.letterboxd.model;

import java.util.List;

// https://api-docs.letterboxd.com/#/schemas/FilmContributions
public class LbFilmContributions {
  public LbContributionType type;
  public List<LbContributorSummary> contributors;
}
