package jimlind.filmlinkd.system.letterboxd.model;

import java.util.List;

// https://api-docs.letterboxd.com/#/schemas/MemberStatistics
public class LbMemberStatistics {
  public LbMemberIdentifier member;
  public LbMemberStatisticsCounts counts;
  public List<LbRatingsHistogramBar> ratingsHistogram;
  public List<Integer> yearsInReview;
}
