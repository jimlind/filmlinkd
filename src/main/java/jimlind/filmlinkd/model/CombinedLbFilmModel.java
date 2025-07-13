package jimlind.filmlinkd.model;

import jimlind.filmlinkd.system.letterboxd.model.LbFilm;
import jimlind.filmlinkd.system.letterboxd.model.LbFilmStatistics;
import jimlind.filmlinkd.system.letterboxd.model.LbFilmSummary;

/** Combination of enough of Letterboxd film data for appropriate use. */
public class CombinedLbFilmModel {
  public LbFilm film;
  public LbFilmStatistics filmStatistics;
  public LbFilmSummary filmSummary;
}
