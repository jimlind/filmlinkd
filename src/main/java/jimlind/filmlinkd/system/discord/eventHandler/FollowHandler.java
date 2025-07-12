package jimlind.filmlinkd.system.discord.eventHandler;

import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import jimlind.filmlinkd.factory.CommandFactory;
import jimlind.filmlinkd.factory.MessageFactory;
import jimlind.filmlinkd.factory.UserFactory;
import jimlind.filmlinkd.model.Command;
import jimlind.filmlinkd.model.Message;
import jimlind.filmlinkd.model.User;
import jimlind.filmlinkd.system.discord.embedBuilder.FollowEmbedBuilder;
import jimlind.filmlinkd.system.discord.helper.AccountHelper;
import jimlind.filmlinkd.system.discord.helper.ChannelHelper;
import jimlind.filmlinkd.system.google.FirestoreManager;
import jimlind.filmlinkd.system.google.PubSubManager;
import jimlind.filmlinkd.system.letterboxd.api.LogEntriesApi;
import jimlind.filmlinkd.system.letterboxd.model.LBLogEntry;
import jimlind.filmlinkd.system.letterboxd.model.LBMember;
import jimlind.filmlinkd.system.letterboxd.utils.LidComparer;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class FollowHandler implements Handler {
  private final AccountHelper accountHelper;
  private final ChannelHelper channelHelper;
  private final CommandFactory commandFactory;
  private final FollowEmbedBuilder followEmbedBuilder;
  private final FirestoreManager firestoreManager;
  private final LogEntriesApi logEntriesApi;
  private final MessageFactory messageFactory;
  private final PubSubManager pubSubManager;
  private final UserFactory userFactory;

  @Inject
  public FollowHandler(
      AccountHelper accountHelper,
      ChannelHelper channelHelper,
      CommandFactory commandFactory,
      FollowEmbedBuilder followEmbedBuilder,
      FirestoreManager firestoreManager,
      LogEntriesApi logEntriesApi,
      MessageFactory messageFactory,
      PubSubManager pubSubManager,
      UserFactory userFactory) {
    this.accountHelper = accountHelper;
    this.channelHelper = channelHelper;
    this.commandFactory = commandFactory;
    this.followEmbedBuilder = followEmbedBuilder;
    this.firestoreManager = firestoreManager;
    this.logEntriesApi = logEntriesApi;
    this.messageFactory = messageFactory;
    this.pubSubManager = pubSubManager;
    this.userFactory = userFactory;
  }

  @Override
  public void handleEvent(SlashCommandInteractionEvent event) {
    event.deferReply().queue();

    LBMember member = accountHelper.getMember(event);
    if (member == null) {
      event.getHook().sendMessage(NO_RESULTS_FOUND).queue();
      return;
    }

    String channelId = channelHelper.getChannelId(event);
    if (channelId.isBlank()) {
      event.getHook().sendMessage(NO_CHANNEL_FOUND).queue();
      return;
    }

    QueryDocumentSnapshot snapshot = firestoreManager.getUserDocument(member.id);
    User user = null;
    // Create the user in the database if it doesn't exist
    if (snapshot == null) {
      firestoreManager.createUserDocument(member);
    } else {
      user = userFactory.createFromSnapshot(snapshot);
    }

    if (!this.firestoreManager.addUserSubscription(member.id, channelId)) {
      event.getHook().sendMessage("Follow Failed").queue();
      return;
    }

    List<LBLogEntry> logEntryList = this.logEntriesApi.getRecentForUser(member.id, 1);
    if (logEntryList.size() == 1) {
      LBLogEntry logEntry = logEntryList.getFirst();

      Command command = commandFactory.create(Command.Type.FOLLOW, logEntry.owner.id, logEntry.id);
      this.pubSubManager.publishCommand(command);

      Message.PublishSource source = Message.PublishSource.Follow;
      Message message = messageFactory.createFromLogEntry(logEntry, source);
      // Only add the channel if we know that the log entry has already been posted
      // Including a channel id is a signal to the MessageReceiver to only send to one channel
      if (user != null && LidComparer.compare(logEntry.id, user.getMostRecentPrevious()) <= 0) {
        message.channelId = channelId;
      }
      this.pubSubManager.publishLogEntry(message);
    }

    ArrayList<MessageEmbed> messageEmbedList = followEmbedBuilder.setMember(member).build();
    event.getHook().sendMessageEmbeds(messageEmbedList).queue();
  }
}
