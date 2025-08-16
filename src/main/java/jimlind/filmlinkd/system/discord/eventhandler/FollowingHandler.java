package jimlind.filmlinkd.system.discord.eventhandler;

import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import jimlind.filmlinkd.factory.UserFactory;
import jimlind.filmlinkd.model.User;
import jimlind.filmlinkd.system.discord.embedbuilder.FollowingEmbedBuilder;
import jimlind.filmlinkd.system.discord.helper.ChannelHelper;
import jimlind.filmlinkd.system.google.firestore.UserReader;
import jimlind.filmlinkd.system.letterboxd.utils.LidComparer;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/** Handles the /following command to show the users followed in the specified channel. */
public class FollowingHandler implements Handler {
  private final ChannelHelper channelHelper;
  private final FollowingEmbedBuilder followingEmbedBuilder;
  private final UserFactory userFactory;
  private final UserReader userReader;

  /**
   * Constructor for this class.
   *
   * @param channelHelper Service that parses a channel id from a slash event with options
   * @param followingEmbedBuilder Builds the embed for the /following command
   * @param userFactory Builds the user object from a Firestore snapshot
   * @param userReader Handles all read-only queries for user data from Firestore
   */
  @Inject
  FollowingHandler(
      ChannelHelper channelHelper,
      FollowingEmbedBuilder followingEmbedBuilder,
      UserFactory userFactory,
      UserReader userReader) {
    this.channelHelper = channelHelper;
    this.followingEmbedBuilder = followingEmbedBuilder;
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

    List<MessageEmbed> messageEmbedList = followingEmbedBuilder.setUserMap(userMap).build();
    event.getHook().sendMessageEmbeds(messageEmbedList).queue();
  }
}
