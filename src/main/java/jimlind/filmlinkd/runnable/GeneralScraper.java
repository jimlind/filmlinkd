package jimlind.filmlinkd.runnable;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import jimlind.filmlinkd.factory.MessageFactory;
import jimlind.filmlinkd.model.Message;
import jimlind.filmlinkd.system.GeneralUserCache;
import jimlind.filmlinkd.system.google.PubSubManager;
import jimlind.filmlinkd.system.letterboxd.api.LogEntriesApi;
import jimlind.filmlinkd.system.letterboxd.model.LbLogEntry;
import jimlind.filmlinkd.system.letterboxd.model.LbMemberSummary;
import jimlind.filmlinkd.system.letterboxd.utils.LidComparer;
import lombok.extern.slf4j.Slf4j;

/**
 * Scrapes the next page from the GeneralUserCache publishes a message in PubSub to notify the other
 * systems.
 */
@Slf4j
public class GeneralScraper implements Runnable {

  private final GeneralUserCache generalUserCache;
  private final LogEntriesApi logEntriesApi;
  private final MessageFactory messageFactory;
  private final PubSubManager pubSubManager;

  /**
   * Constructor for this class.
   *
   * @param generalUserCache Where we store in memory versions records of latest diary entry
   * @param logEntriesApi Fetches log entry data from Letterboxd API
   * @param messageFactory Builds the message object that is pushed into the PubSub system
   * @param pubSubManager Handles the PubSub system to accept commands and messages
   */
  @Inject
  public GeneralScraper(
      GeneralUserCache generalUserCache,
      LogEntriesApi logEntriesApi,
      MessageFactory messageFactory,
      PubSubManager pubSubManager) {
    this.generalUserCache = generalUserCache;
    this.logEntriesApi = logEntriesApi;
    this.messageFactory = messageFactory;
    this.pubSubManager = pubSubManager;
  }

  @Override
  @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
  public void run() {
    Message.PublishSource source = Message.PublishSource.Normal;
    List<Map.Entry<String, String>> userPage = generalUserCache.getNextPage();
    for (Map.Entry<String, String> entry : userPage) {

      List<String> publishedEntryIdList = new ArrayList<>();
      List<LbLogEntry> logEntryList = logEntriesApi.getRecentForUser(entry.getKey(), 10);

      // TODO:
      //      // Filter out entries that are less than 3 minutes old
      //      if (Date.now() - Date.parse(logEntry.whenCreated) < 180000) {
      //        return false;
      //      }

      logEntryList.stream()
          .filter(logEntry -> 0 < LidComparer.compare(logEntry.id, entry.getValue()))
          .forEach(
              logEntry -> {
                Message message = messageFactory.createFromLogEntry(logEntry, source);
                pubSubManager.publishLogEntry(message);
                publishedEntryIdList.add(logEntry.id);
              });

      if (!publishedEntryIdList.isEmpty()) {
        if (log.isInfoEnabled()) {
          LbMemberSummary owner = getOwner(logEntryList.getFirst());
          log.info("Publishing {}x films from {}", publishedEntryIdList.size(), owner.displayName);
        }
        for (String entryId : publishedEntryIdList) {
          generalUserCache.setIfNewer(entry.getKey(), entryId);
        }
      }
    }
  }

  private LbMemberSummary getOwner(LbLogEntry logEntry) {
    return logEntry.owner;
  }
}
