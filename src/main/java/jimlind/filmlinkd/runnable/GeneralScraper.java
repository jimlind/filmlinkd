package jimlind.filmlinkd.runnable;

import com.google.inject.Inject;
import java.time.Instant;
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
import jimlind.filmlinkd.system.letterboxd.utils.DateUtils;
import jimlind.filmlinkd.system.letterboxd.utils.LidComparer;
import lombok.extern.slf4j.Slf4j;

/**
 * Scrapes the next page from the GeneralUserCache publishes a message in PubSub to notify the other
 * systems.
 */
@Slf4j
public class GeneralScraper implements Runnable {
  private final DateUtils dateUtils;
  private final GeneralUserCache generalUserCache;
  private final LogEntriesApi logEntriesApi;
  private final MessageFactory messageFactory;
  private final PubSubManager pubSubManager;

  /**
   * Constructor for this class.
   *
   * @param dateUtils Utilities to translate Letterboxd date strings to other formats
   * @param generalUserCache Where we store in memory versions records of latest diary entry
   * @param logEntriesApi Fetches log entry data from Letterboxd API
   * @param messageFactory Builds the message object that is pushed into the PubSub system
   * @param pubSubManager Handles the PubSub system to accept commands and messages
   */
  @Inject
  public GeneralScraper(
      DateUtils dateUtils,
      GeneralUserCache generalUserCache,
      LogEntriesApi logEntriesApi,
      MessageFactory messageFactory,
      PubSubManager pubSubManager) {
    this.dateUtils = dateUtils;
    this.generalUserCache = generalUserCache;
    this.logEntriesApi = logEntriesApi;
    this.messageFactory = messageFactory;
    this.pubSubManager = pubSubManager;
  }

  @Override
  @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
  public void run() {
    long current = Instant.now().toEpochMilli();
    Message.PublishSource source = Message.PublishSource.Normal;
    List<Map.Entry<String, String>> userPage = generalUserCache.getNextPage();
    for (Map.Entry<String, String> entry : userPage) {
      String entryLetterboxdId = logEntriesApi.getMostRecentEntryLetterboxdId(entry.getKey());
      if (0 >= LidComparer.compare(entryLetterboxdId, entry.getValue())) {
        continue;
      }

      List<String> publishedEntryIdList = new ArrayList<>();
      List<LbLogEntry> logEntryList = logEntriesApi.getRecentForUser(entry.getKey(), 10);

      logEntryList.stream()
          .filter(
              // Filter out entries that are newer than 3 minutes
              logEntry -> current - dateUtils.toMilliseconds(logEntry.getWhenCreated()) >= 180000)
          .filter(
              // Filter out entries that are not newer than the most recent entry
              logEntry -> 0 < LidComparer.compare(logEntry.id, entry.getValue()))
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
        // Set the values in the general user cache outside the steam to avoid concurrency issues
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
