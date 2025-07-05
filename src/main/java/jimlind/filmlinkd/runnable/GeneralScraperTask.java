package jimlind.filmlinkd.runnable;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import jimlind.filmlinkd.factory.MessageFactory;
import jimlind.filmlinkd.model.Message;
import jimlind.filmlinkd.system.UserCache;
import jimlind.filmlinkd.system.google.PubSubManager;
import jimlind.filmlinkd.system.letterboxd.api.LogEntriesAPI;
import jimlind.filmlinkd.system.letterboxd.model.LBLogEntry;
import jimlind.filmlinkd.system.letterboxd.utils.LidComparer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GeneralScraperTask implements Runnable {

  private final UserCache userCache;
  private final LogEntriesAPI logEntriesAPI;
  private final MessageFactory messageFactory;
  private final PubSubManager pubSubManager;

  @Inject
  public GeneralScraperTask(
      UserCache userCache,
      LogEntriesAPI logEntriesAPI,
      MessageFactory messageFactory,
      PubSubManager pubSubManager) {
    this.userCache = userCache;
    this.logEntriesAPI = logEntriesAPI;
    this.messageFactory = messageFactory;
    this.pubSubManager = pubSubManager;
  }

  @Override
  public void run() {
    Message.PublishSource source = Message.PublishSource.Normal;
    List<Map.Entry<String, String>> userPage = userCache.getNextPage();
    for (Map.Entry<String, String> entry : userPage) {

      ArrayList<String> publishedEntryIdList = new ArrayList<String>();

      List<LBLogEntry> logEntryList = logEntriesAPI.getRecentForUser(entry.getKey(), 10);
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
          userCache.setIfNewer(entry.getKey(), entryId);
        }
      }
    }
  }
}
