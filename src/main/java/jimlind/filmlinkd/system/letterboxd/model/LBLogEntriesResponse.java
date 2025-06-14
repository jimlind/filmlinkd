package jimlind.filmlinkd.system.letterboxd.model;

import java.util.List;

// https://api-docs.letterboxd.com/#/schemas/LogEntriesResponse
public class LBLogEntriesResponse {
  public String next;
  public List<LBLogEntry> items;
  public int itemCount;
}
