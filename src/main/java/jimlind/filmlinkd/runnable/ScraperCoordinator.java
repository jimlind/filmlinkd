package jimlind.filmlinkd.runnable;

import com.google.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import jimlind.filmlinkd.factory.MessageFactory;
import jimlind.filmlinkd.model.Message;
import jimlind.filmlinkd.scraper.cache.BaseUserCache;
import jimlind.filmlinkd.system.google.pubsub.PubSubManager;
import jimlind.filmlinkd.system.letterboxd.api.LogEntriesApi;
import jimlind.filmlinkd.system.letterboxd.model.LbLogEntry;
import jimlind.filmlinkd.system.letterboxd.model.LbMemberSummary;
import jimlind.filmlinkd.system.letterboxd.utils.DateUtils;
import jimlind.filmlinkd.system.letterboxd.utils.LidComparer;
import jimlind.filmlinkd.system.letterboxd.web.LogEntryWeb;
import jimlind.filmlinkd.system.letterboxd.web.UserFeedRss;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/** Coordinate all the API checking to Pub/Sub publishing actions. */
@Setter
@Slf4j
public class ScraperCoordinator implements Runnable {
  private final DateUtils dateUtils;
  private final LogEntriesApi logEntriesApi;
  private final LogEntryWeb logEntryWeb;
  private final MessageFactory messageFactory;
  private final PubSubManager pubSubManager;
  private final UserFeedRss userFeedRss;

  private Semaphore semaphore;
  private BaseUserCache userCache;
  private String userLetterboxdId = "";
  private String diaryEntryLetterboxdId = "";
  private Message.PublishSource source;
  private boolean scrapeEntryWithRss;

  /**
   * Constructor for this class.
   *
   * @param dateUtils Utilities to translate Letterboxd date strings to other formats
   * @param logEntriesApi Fetches log entry data from Letterboxd API
   * @param messageFactory Builds the message object that is pushed into the PubSub system
   * @param pubSubManager Handles the PubSub system to accept commands and messages
   */
  @Inject
  ScraperCoordinator(
      DateUtils dateUtils,
      LogEntriesApi logEntriesApi,
      LogEntryWeb logEntryWeb,
      MessageFactory messageFactory,
      PubSubManager pubSubManager,
      UserFeedRss userFeedRss) {
    this.dateUtils = dateUtils;
    this.logEntriesApi = logEntriesApi;
    this.logEntryWeb = logEntryWeb;
    this.messageFactory = messageFactory;
    this.pubSubManager = pubSubManager;
    this.userFeedRss = userFeedRss;
  }

  private static LbMemberSummary getOwner(LbLogEntry logEntry) {
    return logEntry.owner;
  }

  @Override
  public void run() {
    try {
      semaphore.acquire();
      scrape();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } finally {
      semaphore.release();
    }
  }

  /**
   * The method that actually does the scraping. Split into convenience so it can be wrapped with a
   * try/catch
   */
  public void scrape() {
    // Current timestamp
    long current = Instant.now().toEpochMilli();

    // If the most recent diary entry is the same as the input then exit early.
    String mostRecentEntryLid;
    if (scrapeEntryWithRss) {
      String uri = userFeedRss.getMostRecentDiaryLinkFromLid(userLetterboxdId);
      if (uri.isBlank()) {
        return;
      }
      mostRecentEntryLid = logEntryWeb.getLidFromLogEntryPath(uri);
    } else {
      mostRecentEntryLid = logEntriesApi.getMostRecentEntryLetterboxdId(userLetterboxdId);
    }

    if (0 >= LidComparer.compare(mostRecentEntryLid, diaryEntryLetterboxdId)) {
      return;
    }

    // If the LID for the entry is 1 character (or less) that users has not successfully been
    // posted from previously so limit the count to 1 entry.
    int count = diaryEntryLetterboxdId.length() <= 1 ? 1 : 10;
    List<LbLogEntry> logEntryList = logEntriesApi.getRecentForUser(userLetterboxdId, count);
    List<String> publishedEntryIdList = new ArrayList<>();

    logEntryList.stream()
        .filter(
            // Filter out entries that are newer than 3 minutes
            logEntry -> current - dateUtils.toMilliseconds(logEntry.getWhenCreated()) >= 180000)
        .filter(
            // Filter out entries that are not newer than the most recent entry
            logEntry -> 0 < LidComparer.compare(logEntry.id, diaryEntryLetterboxdId))
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
            .setMessage("Publishing {}x films from {} ( {} : {} )")
            .addArgument(publishedEntryIdList.size())
            .addArgument(owner.displayName)
            .addArgument(owner.id)
            .addArgument(owner.username)
            .addKeyValue("source", source)
            .log();
      }
      // Set the values in the general user cache outside the steam to avoid concurrency issues
      for (String entryId : publishedEntryIdList) {
        userCache.setIfNewer(userLetterboxdId, entryId);
      }
    }
  }
}
