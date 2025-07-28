package jimlind.filmlinkd.system.letterboxd.model;

import lombok.Getter;

/**
 * Implements the <a
 * href="https://api-docs.letterboxd.com/#/schemas/ListEntrySummary">ListEntrySummary</a> schema
 * model.
 */
@Getter
public class LbListEntrySummary {
  public int rank;
  public LbFilmSummary film;
}
