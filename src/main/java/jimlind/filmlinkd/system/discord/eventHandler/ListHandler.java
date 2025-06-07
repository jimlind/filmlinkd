package jimlind.filmlinkd.system.discord.eventHandler;
/*
import java.util.ArrayList;
import jimlind.filmlinkd.factory.messageEmbed.ListEmbedFactory;
import jimlind.filmlinkd.system.discord.AccountHelper;
import jimlind.filmlinkd.system.letterboxd.api.ListAPI;
import jimlind.filmlinkd.system.letterboxd.model.LBListSummary;
import jimlind.filmlinkd.system.letterboxd.model.LBListsResponse;
import jimlind.filmlinkd.system.letterboxd.model.LBMember;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ListHandler implements Handler {
  @Autowired private AccountHelper accountHelper;
  @Autowired private ListAPI listAPI;
  @Autowired private ListEmbedFactory listEmbedFactory;

  public String getEventName() {
    return "list";
  }

  public void handleEvent(SlashCommandInteractionEvent event) {
    event.deferReply().queue();

    LBMember member = this.accountHelper.getMember(event);
    if (member == null) {
      event.getHook().sendMessage(NO_RESULTS_FOUND).queue();
      return;
    }

    OptionMapping listOptionMapping = event.getInteraction().getOption("list-name");
    String listNameString = listOptionMapping != null ? listOptionMapping.getAsString() : "";

    String cleanListName = listNameString.toLowerCase().replaceAll("[^a-z0-9]+", "");

    LBListSummary foundList = null;
    LBListsResponse listsResponse = this.listAPI.fetch(member.id, 50);
    if (listsResponse == null) {
      event.getHook().sendMessage("No Results Found").queue();
      return;
    }

    for (LBListSummary item : listsResponse.items) {
      if (cleanListName.equals(item.name.toLowerCase().replaceAll("[^a-z0-9]+", ""))) {
        foundList = item;
      }
    }

    if (foundList == null) {
      event.getHook().sendMessage("No Results Found").queue();
      return;
    }

    ArrayList<MessageEmbed> messageEmbedList = this.listEmbedFactory.create(foundList);
    event.getHook().sendMessageEmbeds(messageEmbedList).queue();
  }
}
*/
