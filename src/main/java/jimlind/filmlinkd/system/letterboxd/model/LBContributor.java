package jimlind.filmlinkd.system.letterboxd.model;

import java.util.List;

// https://api-docs.letterboxd.com/#/schemas/Contributor
public class LBContributor {
  public String id;
  public String name;
  public String tmbid;
  public String bio;
  public LBContributorStatistics statistics;
  public List<LBLink> links;
}
