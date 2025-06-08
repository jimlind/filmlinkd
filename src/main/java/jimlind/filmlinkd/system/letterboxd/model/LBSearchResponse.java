package jimlind.filmlinkd.system.letterboxd.model;

import java.util.List;

// https://api-docs.letterboxd.com/#/schemas/SearchResponse
public class LBSearchResponse {
  public String next;
  public int itemCount;
  public List<LBAbstractSearchItem> items;
}
