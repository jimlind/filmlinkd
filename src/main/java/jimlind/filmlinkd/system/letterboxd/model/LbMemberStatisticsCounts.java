package jimlind.filmlinkd.system.letterboxd.model;

import lombok.Getter;

/**
 * Implements the <a
 * href="https://api-docs.letterboxd.com/#/schemas/MemberStatisticsCounts">MemberStatisticsCounts</a>
 * schema model.
 */
@Getter
public class LbMemberStatisticsCounts {
  public int filmLikes;
  public int listLikes;
  public int reviewLikes;
  public int storyLikes;
  public int watches;
  public int ratings;
  public int reviews;
  public int diaryEntries;
  public int diaryEntriesThisYear;
  public int filmsInDiaryThisYear;
  public int filmsInDiaryLastYear;
  public int watchlist;
  public int lists;
  public int unpublishedLists;
  public int accessedSharedLists;
  public int followers;
  public int following;
  public int listTags;
  public int filmTags;
}
