package jimlind.filmlinkd.system.discord.eventHandler;

import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import jimlind.filmlinkd.factory.UserFactory;
import jimlind.filmlinkd.model.User;
import jimlind.filmlinkd.system.discord.ChannelHelper;
import jimlind.filmlinkd.system.discord.embedBuilder.FollowingEmbedBuilder;
import jimlind.filmlinkd.system.google.FirestoreManager;
import jimlind.filmlinkd.system.letterboxd.utils.LidComparer;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class FollowingHandler implements Handler {
  private final FirestoreManager firestoreManager;
  private final FollowingEmbedBuilder followingEmbedBuilder;
  private final UserFactory userFactory;

  @Inject
  FollowingHandler(
      FirestoreManager firestoreManager,
      FollowingEmbedBuilder followingEmbedBuilder,
      UserFactory userFactory) {
    this.firestoreManager = firestoreManager;
    this.followingEmbedBuilder = followingEmbedBuilder;
    this.userFactory = userFactory;
  }

  @Override
  public void handleEvent(SlashCommandInteractionEvent event) {
    event.deferReply().queue();

    String channelId = ChannelHelper.getChannelId(event);
    if (channelId.isBlank()) {
      event.getHook().sendMessage(NO_CHANNEL_FOUND).queue();
      return;
    }

    List<QueryDocumentSnapshot> documentList =
        this.firestoreManager.getUserDocumentListByChannelId(channelId);

    TreeMap<String, User> userMap = new TreeMap<>(LidComparer::compare);
    for (QueryDocumentSnapshot snapshot : documentList) {
      User user = userFactory.createFromSnapshot(snapshot);
      if (user != null) {
        userMap.put(user.letterboxdId, user);
      }
    }

    ArrayList<MessageEmbed> messageEmbedList = followingEmbedBuilder.setUserMap(userMap).build();
    event.getHook().sendMessageEmbeds(messageEmbedList).queue();
  }
}
