package jimlind.filmlinkd.system.letterboxd.model;

import lombok.Getter;

/**
 * Implements the <a
 * href="https://api-docs.letterboxd.com/#/schemas/ContributionStatistics">ContributionStatistics</a>
 * schema model.
 */
@Getter
public class LbContributionStatistics {
  public int filmCount;
  public LbContributionType type;
}
