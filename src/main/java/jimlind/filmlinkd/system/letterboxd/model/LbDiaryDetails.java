package jimlind.filmlinkd.system.letterboxd.model;

import lombok.Getter;

/**
 * Implements the <a href="https://api-docs.letterboxd.com/#/schemas/DiaryDetails">DiaryDetails</a>
 * schema model.
 */
@Getter
public class LbDiaryDetails {
  public String diaryDate;
  public boolean rewatch;
}
