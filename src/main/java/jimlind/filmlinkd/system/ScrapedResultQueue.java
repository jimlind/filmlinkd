package jimlind.filmlinkd.system;

import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.LinkedList;
import jimlind.filmlinkd.model.ScrapedResult;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
// Queue exists so that I can rate limit the amount of processing that happens.
// If we let every PubSub event trigger some logic it can take over the
// CPU really quickly.
//
// If there was some kind of throttled events available I wouldn't need to do
// this. I should go look that up and see if it does exist.
public class ScrapedResultQueue {
  private final LinkedList<ScrapedResult> scrapedResultList = new LinkedList<ScrapedResult>();
  private final ArrayList<Integer> fetchIdList = new ArrayList<Integer>();

  public void set(ScrapedResult scrapedResult) {
    scrapedResultList.add(scrapedResult);
  }

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
    } catch (Exception e) {
      return null;
    }

    // Indicate the fetch Id is used
    this.fetchIdList.add(fetchClientId);

    // Remove the first message because it's been fetched by all parties
    if (this.fetchIdList.size() == fetchClientTotal) {
      scrapedResultList.removeFirst();
      this.fetchIdList.clear();
    }

    return scrapedResult;
  }
}
