package jimlind.filmlinkd.system.letterboxd.model;

import java.util.List;
import lombok.Getter;

/**
 * Implements the <a
 * href="https://api-docs.letterboxd.com/#/schemas/SearchResponse">SearchResponse</a> schema model.
 */
@Getter
public class LbSearchResponse {
  public String next;
  public int itemCount;
  public List<LbAbstractSearchItem> items;
}
