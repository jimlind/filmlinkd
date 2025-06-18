package jimlind.filmlinkd.system.discord.eventHandler;
/*
import java.util.ArrayList;
import jimlind.filmlinkd.factory.messageEmbed.UnfollowEmbedFactory;
import jimlind.filmlinkd.system.discord.helper.AccountHelper;
import jimlind.filmlinkd.system.discord.helper.ChannelHelper;
import jimlind.filmlinkd.system.google.FirestoreManager;
import jimlind.filmlinkd.system.letterboxd.model.LBMember;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UnfollowHandler implements Handler {
  @Autowired private AccountHelper accountHelper;
  @Autowired private FirestoreManager firestoreManager;
  @Autowired private UnfollowEmbedFactory unfollowEmbedFactory;

  public String getEventName() {
    return "unfollow";
  }

  public void handleEvent(SlashCommandInteractionEvent event) {
    event.deferReply().queue();

    LBMember member = this.accountHelper.getMember(event);
    if (member == null) {
      event.getHook().sendMessage(NO_RESULTS_FOUND).queue();
      return;
    }

    String channelId = ChannelHelper.getChannelId(event);
    if (channelId.isBlank()) {
      event.getHook().sendMessage(NO_CHANNEL_FOUND).queue();
      return;
    }

    if (!this.firestoreManager.removeUserSubscription(member.id, channelId)) {
      event.getHook().sendMessage("Unfollow Failed").queue();
      return;
    }

    ArrayList<MessageEmbed> messageEmbedList = this.unfollowEmbedFactory.create(member);
    event.getHook().sendMessageEmbeds(messageEmbedList).queue();
  }
}
*/
