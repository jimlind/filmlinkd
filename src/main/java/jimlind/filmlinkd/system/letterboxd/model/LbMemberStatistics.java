package jimlind.filmlinkd.system.letterboxd.model;

import java.util.List;

/**
 * Implements the <a
 * href="https://api-docs.letterboxd.com/#/schemas/MemberStatistics">MemberStatistics</a> schema
 * model.
 */
public class LbMemberStatistics {
  public LbMemberIdentifier member;
  public LbMemberStatisticsCounts counts;
  public List<LbRatingsHistogramBar> ratingsHistogram;
  public List<Integer> yearsInReview;
}
