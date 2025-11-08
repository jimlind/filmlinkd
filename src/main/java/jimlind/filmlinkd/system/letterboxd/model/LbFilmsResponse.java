package jimlind.filmlinkd.system.letterboxd.model;

import java.util.List;
import lombok.Getter;

/**
 * Implements the <a
 * href="https://api-docs.letterboxd.com/#/schemas/FilmsResponse">FilmsResponse</a> schema model.
 */
@Getter
public class LbFilmsResponse {
  public String next;
  public List<LbFilmSummary> items;
  public int itemCount;
}
