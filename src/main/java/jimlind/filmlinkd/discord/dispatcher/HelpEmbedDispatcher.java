package jimlind.filmlinkd.discord.dispatcher;

import com.google.inject.Inject;
import jimlind.filmlinkd.core.scheduling.TimedTaskRunner;
import jimlind.filmlinkd.discord.embed.factory.HelpEmbedFactory;
import jimlind.filmlinkd.factory.MessageFactory;
import jimlind.filmlinkd.model.Message;
import jimlind.filmlinkd.system.google.pubsub.PubSubManager;
import jimlind.filmlinkd.system.letterboxd.api.LogEntriesApi;
import jimlind.filmlinkd.system.letterboxd.model.LbLogEntry;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.exceptions.PermissionException;

/**
 * Executes the timed help embed tasks in a way that appropriately uses the TimedTaskRunner base to
 * ensure that things are closed when not needed.
 */
@Slf4j
public class HelpEmbedDispatcher extends TimedTaskRunner {
  private static final int MAX_TEST_MESSAGES = 5;
  private static final int INITIAL_DELAY_MILLISECONDS = 2000;
  private static final int INTERVAL_MILLISECONDS = 1000;

  private final HelpEmbedFactory helpEmbedFactory;
  private final LogEntriesApi logEntriesApi;
  private final MessageFactory messageFactory;
  private final PubSubManager pubSubManager;

  private MessageChannel messageChannel;
  private int count = 1;

  /**
   * Constructor for this class.
   *
   * @param helpEmbedFactory Builds the embed for the /help command
   * @param logEntriesApi Fetches log entry data from Letterboxd API
   * @param messageFactory Builds the message object that is pushed into the PubSub system
   * @param pubSubManager Handles the PubSub system to accept commands and messages
   */
  @Inject
  public HelpEmbedDispatcher(
      HelpEmbedFactory helpEmbedFactory,
      LogEntriesApi logEntriesApi,
      MessageFactory messageFactory,
      PubSubManager pubSubManager) {
    super(INITIAL_DELAY_MILLISECONDS, INTERVAL_MILLISECONDS);

    this.helpEmbedFactory = helpEmbedFactory;
    this.logEntriesApi = logEntriesApi;
    this.messageFactory = messageFactory;
    this.pubSubManager = pubSubManager;
  }

  /**
   * Configure the HelpEmbedDispatcher. This public configure method is used so that the actual
   * constructor can get dependencies properly injected. If this pattern becomes common I should
   * make some factories to avoid the edge case I'm creating.
   *
   * @param messageChannel The channel to send the help messages to.
   */
  public void configure(MessageChannel messageChannel) {
    this.messageChannel = messageChannel;
  }

  @Override
  protected void runTask() {
    if (messageChannel == null) {
      String message = "Attempting to run the HelpEmbedDispatcher without a channel configured";
      log.atError().setMessage(message).log();
      this.stop();
    }

    try {
      messageChannel.sendMessageEmbeds(helpEmbedFactory.createTestMessage(count)).queue();
    } catch (IllegalStateException | PermissionException e) {
      log.atInfo()
          .setMessage("Unable to create or send test message")
          .addKeyValue("channel", messageChannel)
          .setCause(e)
          .log();
    }
    if (count == MAX_TEST_MESSAGES) {
      sendSingleDiaryEntry(messageChannel.getId());
    }
    count++;
  }

  @Override
  protected boolean shouldStop() {
    return count > MAX_TEST_MESSAGES;
  }

  /**
   * This sends the most recent entry for a user to a specific channel to validate for users that it
   * works as expected. Maybe it could be more generalized and put in a utility class but that is
   * necessary right now.
   *
   * @param channelId ChannelId to send a diary entry to.
   */
  private void sendSingleDiaryEntry(String channelId) {
    LbLogEntry logEntry = logEntriesApi.getRecentForUser("1e4Ab", 1).getFirst();
    Message message = messageFactory.createFromLogEntry(logEntry, Message.PublishSource.Help);
    message.setChannelId(channelId);
    pubSubManager.publishLogEntry(message);
  }
}
