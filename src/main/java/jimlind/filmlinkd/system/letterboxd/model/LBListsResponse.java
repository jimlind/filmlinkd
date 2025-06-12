package jimlind.filmlinkd.system.letterboxd.model;

import java.util.List;

// https://api-docs.letterboxd.com/#/schemas/ListsResponse
public class LBListsResponse {
  public String next;
  public int itemCount;
  public List<LBListSummary> items;
}
