package jimlind.filmlinkd.discord.commands.following;

import java.util.List;
import javax.inject.Inject;
import jimlind.filmlinkd.database.user.UserDao;
import jimlind.filmlinkd.database.user.UserRow;
import jimlind.filmlinkd.discord.event.handler.Handler;
import jimlind.filmlinkd.system.discord.helper.ChannelHelper;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/** Handles the /following command to show the users followed in the specified channel. */
public class FollowingHandler implements Handler {
  private final FollowingEmbedFactory followingEmbedFactory;
  private final UserDao userDao;

  /**
   * Constructor for this class.
   *
   * @param followingEmbedFactory Builds the embed for the /following command
   * @param userDao Handles queries and inserts for user data in the local database
   */
  @Inject
  FollowingHandler(FollowingEmbedFactory followingEmbedFactory, UserDao userDao) {
    this.followingEmbedFactory = followingEmbedFactory;
    this.userDao = userDao;
  }

  @Override
  public void handleEvent(SlashCommandInteractionEvent event) {
    event.deferReply().queue();

    Long channelId = ChannelHelper.getChannelId(event);
    if (channelId == null) {
      event.getHook().sendMessage(NO_CHANNEL_FOUND).queue();
      return;
    }

    List<UserRow> userRowList = userDao.getUserListByChannelId(channelId);
//
//    Map<String, FirestoreUser> userMap = new TreeMap<>(LidComparer::compare);
//    for (QueryDocumentSnapshot snapshot : documentList) {
//      FirestoreUser user = userFactory.createFromSnapshot(snapshot);
//      if (user != null) {
//        userMap.put(user.letterboxdId, user);
//      }
//    }

    List<MessageEmbed> messageEmbedList = followingEmbedFactory.create(userRowList);
    event.getHook().sendMessageEmbeds(messageEmbedList).queue();
  }
}
