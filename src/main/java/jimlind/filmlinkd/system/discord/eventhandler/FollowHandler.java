package jimlind.filmlinkd.system.discord.eventhandler;

import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.inject.Inject;
import java.util.List;
import jimlind.filmlinkd.discord.factory.FollowEmbedFactory;
import jimlind.filmlinkd.factory.CommandFactory;
import jimlind.filmlinkd.factory.MessageFactory;
import jimlind.filmlinkd.factory.UserFactory;
import jimlind.filmlinkd.model.Command;
import jimlind.filmlinkd.model.Message;
import jimlind.filmlinkd.model.User;
import jimlind.filmlinkd.system.discord.helper.AccountHelper;
import jimlind.filmlinkd.system.discord.helper.ChannelHelper;
import jimlind.filmlinkd.system.google.PubSubManager;
import jimlind.filmlinkd.system.google.firestore.UserReader;
import jimlind.filmlinkd.system.google.firestore.UserWriter;
import jimlind.filmlinkd.system.letterboxd.api.LogEntriesApi;
import jimlind.filmlinkd.system.letterboxd.model.LbLogEntry;
import jimlind.filmlinkd.system.letterboxd.model.LbMember;
import jimlind.filmlinkd.system.letterboxd.model.LbMemberSummary;
import jimlind.filmlinkd.system.letterboxd.utils.LidComparer;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/** Handles the /follow command to follow a user and show new diary entries in specified channel. */
public class FollowHandler implements Handler {
  private static final int EXPECTED_SINGLE_RESULT = 1;
  private final AccountHelper accountHelper;
  private final ChannelHelper channelHelper;
  private final CommandFactory commandFactory;
  private final FollowEmbedFactory followEmbedFactory;
  private final LogEntriesApi logEntriesApi;
  private final MessageFactory messageFactory;
  private final PubSubManager pubSubManager;
  private final UserFactory userFactory;
  private final UserReader userReader;
  private final UserWriter userWriter;

  /**
   * Constructor for this class.
   *
   * @param accountHelper Handles translating account names to proper data models
   * @param channelHelper Service that parses a channel id from a slash event with options
   * @param commandFactory Builds the command object that is pushed into the PubSub system
   * @param followEmbedFactory Builds the embed for the /follow command
   * @param logEntriesApi Fetches log entry data from Letterboxd API
   * @param messageFactory Builds the message object that is pushed into the PubSub system
   * @param pubSubManager Handles the PubSub system to accept commands and messages
   * @param userFactory Builds the user object from a Firestore snapshot
   * @param userReader Handles all read-only queries for user data from Firestore
   * @param userWriter Handles all write operations for user data in Firestore
   */
  @Inject
  public FollowHandler(
      AccountHelper accountHelper,
      ChannelHelper channelHelper,
      CommandFactory commandFactory,
      FollowEmbedFactory followEmbedFactory,
      LogEntriesApi logEntriesApi,
      MessageFactory messageFactory,
      PubSubManager pubSubManager,
      UserFactory userFactory,
      UserReader userReader,
      UserWriter userWriter) {
    this.accountHelper = accountHelper;
    this.channelHelper = channelHelper;
    this.commandFactory = commandFactory;
    this.followEmbedFactory = followEmbedFactory;
    this.logEntriesApi = logEntriesApi;
    this.messageFactory = messageFactory;
    this.pubSubManager = pubSubManager;
    this.userFactory = userFactory;
    this.userReader = userReader;
    this.userWriter = userWriter;
  }

  private static LbMemberSummary getOwner(LbLogEntry logEntry) {
    return logEntry.getOwner();
  }

  @Override
  public void handleEvent(SlashCommandInteractionEvent event) {
    event.deferReply().queue();

    LbMember member = accountHelper.getMember(event);
    if (member == null) {
      event.getHook().sendMessage(NO_RESULTS_FOUND).queue();
      return;
    }

    String channelId = channelHelper.getChannelId(event);
    if (channelId.isBlank()) {
      event.getHook().sendMessage(NO_CHANNEL_FOUND).queue();
      return;
    }

    QueryDocumentSnapshot snapshot = userReader.getUserDocument(member.id);
    User user = null;
    // Create the user in the database if it doesn't exist
    if (snapshot == null) {
      userWriter.createUserDocument(member);
    } else {
      user = userFactory.createFromSnapshot(snapshot);
    }

    if (!userWriter.addUserSubscription(member.id, channelId)) {
      event.getHook().sendMessage("Follow Failed").queue();
      return;
    }

    List<LbLogEntry> logEntryList = this.logEntriesApi.getRecentForUser(member.id, 1);
    if (logEntryList.size() == EXPECTED_SINGLE_RESULT) {
      LbLogEntry logEntry = logEntryList.getFirst();

      Command command =
          commandFactory.create(Command.Type.FOLLOW, getOwner(logEntry).id, logEntry.id);
      this.pubSubManager.publishCommand(command);

      Message.PublishSource source = Message.PublishSource.Follow;
      Message message = messageFactory.createFromLogEntry(logEntry, source);
      // Only add the channel if we know that the log entry has already been posted
      // Including a channel id is a signal to the LogEntryMessageReceiver to only send to one
      // channel
      if (user != null && LidComparer.compare(logEntry.id, user.getMostRecentPrevious()) <= 0) {
        message.setChannelId(channelId);
      }
      this.pubSubManager.publishLogEntry(message);
    }

    List<MessageEmbed> messageEmbedList = followEmbedFactory.setMember(member).build();
    event.getHook().sendMessageEmbeds(messageEmbedList).queue();
  }
}
