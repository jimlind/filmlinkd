package jimlind.filmlinkd.runnable;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import jimlind.filmlinkd.factory.MessageFactory;
import jimlind.filmlinkd.model.Message;
import jimlind.filmlinkd.system.GeneralUserCache;
import jimlind.filmlinkd.system.google.PubSubManager;
import jimlind.filmlinkd.system.letterboxd.api.LogEntriesAPI;
import jimlind.filmlinkd.system.letterboxd.model.LBLogEntry;
import jimlind.filmlinkd.system.letterboxd.utils.LidComparer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GeneralScraper implements Runnable {

  private final GeneralUserCache generalUserCache;
  private final LogEntriesAPI logEntriesAPI;
  private final MessageFactory messageFactory;
  private final PubSubManager pubSubManager;

  @Inject
  public GeneralScraper(
      GeneralUserCache generalUserCache,
      LogEntriesAPI logEntriesAPI,
      MessageFactory messageFactory,
      PubSubManager pubSubManager) {
    this.generalUserCache = generalUserCache;
    this.logEntriesAPI = logEntriesAPI;
    this.messageFactory = messageFactory;
    this.pubSubManager = pubSubManager;
  }

  @Override
  public void run() {
    Message.PublishSource source = Message.PublishSource.Normal;
    List<Map.Entry<String, String>> userPage = generalUserCache.getNextPage();
    for (Map.Entry<String, String> entry : userPage) {

      ArrayList<String> publishedEntryIdList = new ArrayList<String>();

      List<LBLogEntry> logEntryList = logEntriesAPI.getRecentForUser(entry.getKey(), 10);

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
          String name = logEntryList.getFirst().owner.displayName;
          log.info("Publishing {}x films from {}", publishedEntryIdList.size(), name);
        }
        for (String entryId : publishedEntryIdList) {
          generalUserCache.setIfNewer(entry.getKey(), entryId);
        }
      }
    }
  }
}
