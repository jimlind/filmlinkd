package jimlind.filmlinkd.system.discord.eventHandler;
/*
import com.google.cloud.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import jimlind.filmlinkd.factory.UserFactory;
import jimlind.filmlinkd.factory.messageEmbed.FollowingEmbedFactory;
import jimlind.filmlinkd.model.User;
import jimlind.filmlinkd.system.discord.ChannelHelper;
import jimlind.filmlinkd.system.google.FirestoreManager;
import jimlind.filmlinkd.system.letterboxd.LidComparer;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FollowingHandler implements Handler {
  @Autowired private FirestoreManager firestoreManager;
  @Autowired private FollowingEmbedFactory followingEmbedFactory;
  @Autowired private UserFactory userFactory;

  public String getEventName() {
    return "following";
  }

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
      User user = this.userFactory.createFromSnapshot(snapshot);
      if (user != null) {
        userMap.put(user.letterboxdId, user);
      }
    }

    ArrayList<MessageEmbed> messageEmbedList = this.followingEmbedFactory.create(userMap);
    event.getHook().sendMessageEmbeds(messageEmbedList).queue();
  }
}
*/
