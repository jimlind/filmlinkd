package jimlind.filmlinkd.runnable;

import com.google.inject.Inject;
import jimlind.filmlinkd.cache.GeneralUserCache;
import jimlind.filmlinkd.factory.MessageFactory;
import jimlind.filmlinkd.model.Message;
import jimlind.filmlinkd.system.google.pubsub.PubSubManager;
import jimlind.filmlinkd.system.letterboxd.api.LogEntriesApi;
import jimlind.filmlinkd.system.letterboxd.utils.DateUtils;

/**
 * Scrapes the next page from the GeneralUserCache publishes a message in PubSub with the source set
 * to "normal" to notify the other systems.
 */
public class GeneralScraper extends BaseScraper {
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
  public GeneralScraper(
      DateUtils dateUtils,
      GeneralUserCache userCache,
      LogEntriesApi logEntriesApi,
      MessageFactory messageFactory,
      PubSubManager pubSubManager) {
    super(dateUtils, userCache, logEntriesApi, messageFactory, pubSubManager);
    this.source = Message.PublishSource.Normal;
  }
}
