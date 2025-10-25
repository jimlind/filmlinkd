package jimlind.filmlinkd.discord.event.handler;

import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import jimlind.filmlinkd.discord.embed.factory.FollowingEmbedFactory;
import jimlind.filmlinkd.factory.UserFactory;
import jimlind.filmlinkd.google.db.UserReaderInterface;
import jimlind.filmlinkd.model.User;
import jimlind.filmlinkd.system.discord.helper.ChannelHelper;
import jimlind.filmlinkd.system.letterboxd.utils.LidComparer;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/** Handles the /following command to show the users followed in the specified channel. */
public class FollowingHandler implements Handler {
  private final ChannelHelper channelHelper;
  private final FollowingEmbedFactory followingEmbedFactory;
  private final UserFactory userFactory;
  private final UserReaderInterface userReader;

  /**
   * Constructor for this class.
   *
   * @param channelHelper Service that parses a channel id from a slash event with options
   * @param followingEmbedFactory Builds the embed for the /following command
   * @param userFactory Builds the user object from a Firestore snapshot
   * @param userReader Handles all read-only queries for user data from Firestore
   */
  @Inject
  FollowingHandler(
      ChannelHelper channelHelper,
      FollowingEmbedFactory followingEmbedFactory,
      UserFactory userFactory,
      UserReaderInterface userReader) {
    this.channelHelper = channelHelper;
    this.followingEmbedFactory = followingEmbedFactory;
    this.userFactory = userFactory;
    this.userReader = userReader;
  }

  @Override
  public void handleEvent(SlashCommandInteractionEvent event) {
    event.deferReply().queue();

    String channelId = channelHelper.getChannelId(event);
    if (channelId.isBlank()) {
      event.getHook().sendMessage(NO_CHANNEL_FOUND).queue();
      return;
    }

    List<QueryDocumentSnapshot> documentList = userReader.getUserDocumentListByChannelId(channelId);

    Map<String, User> userMap = new TreeMap<>(LidComparer::compare);
    for (QueryDocumentSnapshot snapshot : documentList) {
      User user = userFactory.createFromSnapshot(snapshot);
      if (user != null) {
        userMap.put(user.letterboxdId, user);
      }
    }

    List<MessageEmbed> messageEmbedList = followingEmbedFactory.create(userMap);
    event.getHook().sendMessageEmbeds(messageEmbedList).queue();
  }
}
