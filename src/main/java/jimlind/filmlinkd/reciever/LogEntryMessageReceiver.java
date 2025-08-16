package jimlind.filmlinkd.reciever;

import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.pubsub.v1.PubsubMessage;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jimlind.filmlinkd.factory.ScrapedResultFactory;
import jimlind.filmlinkd.model.ScrapedResult;
import jimlind.filmlinkd.system.EntryCache;
import jimlind.filmlinkd.system.ScrapedResultQueue;

/** The LogEntryMessageReceiver gets PubSub messages and responds to them appropriately. */
@Singleton
public class LogEntryMessageReceiver implements com.google.cloud.pubsub.v1.MessageReceiver {
  private final EntryCache entryCache;
  private final ScrapedResultFactory scrapedResultFactory;
  private final ScrapedResultQueue scrapedResultQueue;

  /**
   * Constructor for the {@link LogEntryMessageReceiver}.
   *
   * @param entryCache Where we keep a record of most of recent diary entries
   * @param scrapedResultFactory Factory for creating {@link ScrapedResult} model from message data
   * @param scrapedResultQueue Where we put PubSub events to keep from overwhelming the system
   */
  @Inject
  public LogEntryMessageReceiver(
      EntryCache entryCache,
      ScrapedResultFactory scrapedResultFactory,
      ScrapedResultQueue scrapedResultQueue) {
    this.entryCache = entryCache;
    this.scrapedResultFactory = scrapedResultFactory;
    this.scrapedResultQueue = scrapedResultQueue;
  }

  /**
   * This writes to a queue so that I can rate limit the amount of processing that happens. If we
   * let every PubSub event trigger some logic it can take over the CPU really quickly.
   *
   * @param pubsubMessage The message coming from PubSub event containing the payload
   * @param ackReplyConsumer The class needed to acknowledge receiving the message
   */
  @Override
  public void receiveMessage(PubsubMessage pubsubMessage, AckReplyConsumer ackReplyConsumer) {
    // Immediately ack the message. It'll get sent again eventually if it doesn't register.
    ackReplyConsumer.ack();

    String messagePayload = pubsubMessage.getData().toStringUtf8();
    String entryLid = getEntryLid(messagePayload);
    String channelId = getChannelId(messagePayload);

    // We are expecting multiple requests to post a diary entry so we attempt to maintain the one
    // source of truth on the server that sends messages. One mechanism of that is a memory cache.
    if (entryCache.get(entryLid) && channelId.isBlank()) {
      // If the key is in the cache and channelId isn't provided skip it
      return;
    } else {
      // Assume that write will succeed so write to cache. If it actually doesn't succeed then it'll
      // get tried again some later time, but this is designed to limit duplicates from scrape
      // events
      entryCache.set(entryLid);
    }

    ScrapedResult scrapedResult = scrapedResultFactory.createFromPubSubMessage(pubsubMessage);
    if (scrapedResult.shouldBeQueued()) {
      scrapedResultQueue.set(scrapedResult);
    }
  }

  private String getEntryLid(String input) {
    String regex = "\"lid\":\"(\\w+)\"";
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(input);

    if (matcher.find()) {
      return matcher.group(1);
    } else {
      return "";
    }
  }

  private String getChannelId(String input) {
    String regex = "\"channelId\":\"(\\d+)\"";
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(input);

    if (matcher.find()) {
      return matcher.group(1);
    } else {
      return "";
    }
  }
}
