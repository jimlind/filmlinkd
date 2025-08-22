package jimlind.filmlinkd.runnable;

import com.google.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import jimlind.filmlinkd.cache.BaseUserCache;
import jimlind.filmlinkd.factory.MessageFactory;
import jimlind.filmlinkd.model.Message;
import jimlind.filmlinkd.system.google.PubSubManager;
import jimlind.filmlinkd.system.letterboxd.api.LogEntriesApi;
import jimlind.filmlinkd.system.letterboxd.model.LbLogEntry;
import jimlind.filmlinkd.system.letterboxd.model.LbMemberSummary;
import jimlind.filmlinkd.system.letterboxd.utils.DateUtils;
import jimlind.filmlinkd.system.letterboxd.utils.LidComparer;
import lombok.extern.slf4j.Slf4j;

/**
 * Base scraper that gets pages of users from user cache and publishes events to notify other
 * systems.
 */
@Slf4j
public class BaseScraper implements Runnable {
  protected DateUtils dateUtils;
  protected BaseUserCache userCache;
  protected LogEntriesApi logEntriesApi;
  protected MessageFactory messageFactory;
  protected PubSubManager pubSubManager;
  protected Message.PublishSource source;

  /**
   * Constructor for this class.
   *
   * @param dateUtils Utilities to translate Letterboxd date strings to other formats
   * @param userCache Where we store in memory versions records of latest diary entry
   * @param logEntriesApi Fetches log entry data from Letterboxd API
   * @param messageFactory Builds the message object that is pushed into the PubSub system
   * @param pubSubManager Handles the PubSub system to accept commands and messages
   */
  @Inject
  public BaseScraper(
      DateUtils dateUtils,
      BaseUserCache userCache,
      LogEntriesApi logEntriesApi,
      MessageFactory messageFactory,
      PubSubManager pubSubManager) {
    this.dateUtils = dateUtils;
    this.userCache = userCache;
    this.logEntriesApi = logEntriesApi;
    this.messageFactory = messageFactory;
    this.pubSubManager = pubSubManager;
  }

  @Override
  @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
  public void run() {
    long current = Instant.now().toEpochMilli();
    List<Map.Entry<String, String>> userPage = userCache.getNextPage();
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
          log.atInfo()
              .setMessage("Publishing {}x films from {}")
              .addArgument(publishedEntryIdList.size())
              .addArgument(owner.displayName)
              .addKeyValue("source", source)
              .log();
        }
        // Set the values in the general user cache outside the steam to avoid concurrency issues
        for (String entryId : publishedEntryIdList) {
          userCache.setIfNewer(entry.getKey(), entryId);
        }
      }
    }
  }

  /**
   * A way to access the owner of a log entry object allowing looser coupling.
   *
   * @param logEntry The log entry letterboxd object
   * @return The owner of the log entry as letterboxd object
   */
  protected LbMemberSummary getOwner(LbLogEntry logEntry) {
    return logEntry.owner;
  }
}
