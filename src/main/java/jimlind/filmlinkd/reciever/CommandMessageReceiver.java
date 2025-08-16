package jimlind.filmlinkd.reciever;

import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.pubsub.v1.PubsubMessage;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jimlind.filmlinkd.system.GeneralUserCache;
import lombok.extern.slf4j.Slf4j;

/** The CommandMessageReceiver gets PubSub messages and responds to them appropriately. */
@Slf4j
@Singleton
public class CommandMessageReceiver implements com.google.cloud.pubsub.v1.MessageReceiver {
  private final GeneralUserCache generalUserCache;

  /**
   * Constructor for the {@link CommandMessageReceiver}.
   *
   * @param generalUserCache Where we store in memory versions records of latest diary entry
   */
  @Inject
  public CommandMessageReceiver(GeneralUserCache generalUserCache) {
    this.generalUserCache = generalUserCache;
  }

  private static String getUser(String input) {
    return getString(input, "\"user\":\"(\\w+)\"");
  }

  private static String getEntry(String input) {
    return getString(input, "\"entry\":\"(\\w+)\"");
  }

  private static String getString(String input, String regex) {
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(input);

    if (matcher.find()) {
      return matcher.group(1);
    } else {
      return "";
    }
  }

  /**
   * Receives the command message.
   *
   * @param pubsubMessage The message coming from PubSub event containing the payload
   * @param ackReplyConsumer The class needed to acknowledge receiving the message
   */
  @Override
  public void receiveMessage(PubsubMessage pubsubMessage, AckReplyConsumer ackReplyConsumer) {
    // Immediately ack the message. The next refresh will update contents if it fails.
    ackReplyConsumer.ack();

    String messagePayload = pubsubMessage.getData().toStringUtf8();
    String userId = getUser(messagePayload);
    String entryId = getEntry(messagePayload);

    generalUserCache.setIfNewer(userId, entryId);
  }
}
