package jimlind.filmlinkd.system.letterboxd.model;

import java.util.List;
import lombok.Getter;

/**
 * Implements the <a
 * href="https://api-docs.letterboxd.com/#/schemas/MemberStatistics">MemberStatistics</a> schema
 * model.
 */
@Getter
public class LbMemberStatistics {
  public LbMemberIdentifier member;
  public LbMemberStatisticsCounts counts;
  public List<LbRatingsHistogramBar> ratingsHistogram;
  public List<Integer> yearsInReview;
}
