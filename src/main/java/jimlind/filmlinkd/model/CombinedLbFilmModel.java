package jimlind.filmlinkd.model;

import jimlind.filmlinkd.system.letterboxd.model.LbFilm;
import jimlind.filmlinkd.system.letterboxd.model.LbFilmStatistics;
import jimlind.filmlinkd.system.letterboxd.model.LbFilmStatisticsCounts;
import jimlind.filmlinkd.system.letterboxd.model.LbFilmSummary;
import lombok.Getter;
import lombok.Setter;

/** Combination of enough of Letterboxd film data for appropriate use. */
@Getter
@Setter
public class CombinedLbFilmModel {
  public LbFilm film;
  public LbFilmStatistics filmStatistics;
  public LbFilmSummary filmSummary;

  public LbFilmStatisticsCounts getStatisticsCounts() {
    return filmStatistics.counts;
  }
}
