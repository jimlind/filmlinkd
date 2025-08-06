package jimlind.filmlinkd.system;

import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import jimlind.filmlinkd.model.ScrapedResult;
import lombok.extern.slf4j.Slf4j;

/**
 * Queue exists so that I can rate limit the amount of processing that happens. If we let every
 * PubSub event trigger some logic it can take over the CPU really quickly.
 *
 * <p>If there was some kind of throttled events available I wouldn't need to do this. I should go
 * look that up and see if it does exist.
 */
@Singleton
@Slf4j
public class ScrapedResultQueue {
  private final List<ScrapedResult> scrapedResultList = new LinkedList<>();
  private final List<Integer> fetchIdList = new ArrayList<>();

  /**
   * Puts a message in the queue for processing offline from the PubSub system.
   *
   * @param scrapedResult The contents of the message from PubSub
   */
  public void set(ScrapedResult scrapedResult) {
    scrapedResultList.add(scrapedResult);
  }

  /**
   * This method does more than just "get." It retrieves the message contents but also tracks the id
   * of the shard that has already successfully gotten data from this system. If a data was already
   * retrieved it will return null. Once data has been gotten from all clients then it deletes that
   * data from the queue and returns the next message. This runs as synchronized to avoid requests
   * trying to get data while it might need to be cleared.
   *
   * <p>The fact that this comment is that long means there is too much logic in this method.
   *
   * @param fetchClientId This is the shard id
   * @param fetchClientTotal This is the total number of shards
   * @return Returns null if there is nothing to get was already gotten or if the first attempt will
   *     get the message contents
   */
  public synchronized ScrapedResult get(Integer fetchClientId, Integer fetchClientTotal) {
    // Check if the specific ID was used for fetching and set it otherwise
    if (this.fetchIdList.contains(fetchClientId)) {
      return null;
    }

    // Get the first message from the queue
    // Checking length doesn't seem to be a foolproof way to resolve this so wrapping in a try/catch
    ScrapedResult scrapedResult;
    try {
      scrapedResult = scrapedResultList.getFirst();
    } catch (NoSuchElementException e) {
      return null;
    }

    // Indicate the fetch ID is used
    this.fetchIdList.add(fetchClientId);

    // Remove the first message because it's been fetched by all parties
    if (this.fetchIdList.size() == fetchClientTotal) {
      scrapedResultList.removeFirst();
      this.fetchIdList.clear();
    }

    return scrapedResult;
  }
}
