package jimlind.filmlinkd.system.letterboxd.model;

import java.util.List;
import lombok.Getter;

/**
 * Implements the <a
 * href="https://api-docs.letterboxd.com/#/schemas/ListsResponse">ListsResponse</a> schema model.
 */
@Getter
public class LbListsResponse {
  public String next;
  public int itemCount;
  public List<LbListSummary> items;
}
