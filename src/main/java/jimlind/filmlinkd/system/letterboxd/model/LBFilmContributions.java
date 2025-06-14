package jimlind.filmlinkd.system.letterboxd.model;

import java.util.List;

// https://api-docs.letterboxd.com/#/schemas/FilmContributions
public class LBFilmContributions {
  public LBContributionType type;
  public List<LBContributorSummary> contributors;
}
