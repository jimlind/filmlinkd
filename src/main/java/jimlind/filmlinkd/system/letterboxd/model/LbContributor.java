package jimlind.filmlinkd.system.letterboxd.model;

import java.util.List;
import lombok.Getter;

/**
 * Implements the <a href="https://api-docs.letterboxd.com/#/schemas/Contributor">Contributor</a>
 * schema model.
 */
@Getter
public class LbContributor {
  public String id;
  public String name;
  public String tmbid;
  public String bio;
  public LbContributorStatistics statistics;
  public List<LbLink> links;
}
