package jimlind.filmlinkd.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Model representing a ScrapeResult that happens when the scraper announces a new scraped entry.
 */
@Getter
@Setter
public class ScrapedResult {
  public Message message;
  public User user;
}
