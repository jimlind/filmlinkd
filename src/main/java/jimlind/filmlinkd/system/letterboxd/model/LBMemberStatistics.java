package jimlind.filmlinkd.system.letterboxd.model;

import java.util.List;

// https://api-docs.letterboxd.com/#/schemas/MemberStatistics
public class LBMemberStatistics {
  public LBMemberIdentifier member;
  public LBMemberStatisticsCounts counts;
  public List<LBRatingsHistogramBar> ratingsHistogram;
  public List<Integer> yearsInReview;
}
