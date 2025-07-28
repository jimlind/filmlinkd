package jimlind.filmlinkd.system.letterboxd.model;

import java.util.List;
import lombok.Getter;

/**
 * Implements the <a
 * href="https://api-docs.letterboxd.com/#/schemas/ContributorStatistics">ContributorStatistics</a>
 * schema model.
 */
@Getter
public class LbContributorStatistics {
  public List<LbContributionStatistics> contributions;
}
