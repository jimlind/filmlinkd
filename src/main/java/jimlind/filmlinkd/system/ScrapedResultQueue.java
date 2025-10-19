package jimlind.filmlinkd.system;

import com.google.inject.Singleton;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import jimlind.filmlinkd.model.ScrapedResult;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

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

  /**
   * Puts a message in the queue for processing offline from the PubSub system.
   *
   * @param scrapedResult The contents of the message from PubSub
   */
  public void set(ScrapedResult scrapedResult) {
    scrapedResultList.add(scrapedResult);
  }

  /**
   * Gets the first element and removes it from the list.
   *
   * @return Returns null if there is nothing to get or the message contents
   */
  public @Nullable ScrapedResult getFirst() {
    try {
      return scrapedResultList.removeFirst();
    } catch (NoSuchElementException e) {
      return null;
    }
  }
}
