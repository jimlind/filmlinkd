package jimlind.filmlinkd.system.discord.eventHandler;
/*
import java.util.ArrayList;
import jimlind.filmlinkd.factory.messageEmbed.ContributorEmbedFactory;
import jimlind.filmlinkd.system.letterboxd.api.ContributorAPI;
import jimlind.filmlinkd.system.letterboxd.model.LBContributor;
import jimlind.filmlinkd.system.letterboxd.model.LBSearchResponse;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ContributorHandler implements Handler {
  @Autowired private ContributorAPI contributorAPI;
  @Autowired private ContributorEmbedFactory contributorEmbedFactory;

  public String getEventName() {
    return "contributor";
  }

  public void handleEvent(SlashCommandInteractionEvent event) {
    event.deferReply().queue();

    OptionMapping optionMapping = event.getInteraction().getOption("contributor-name");
    String contributorName = optionMapping != null ? optionMapping.getAsString() : "";
    LBSearchResponse searchResponse = this.contributorAPI.fetch(contributorName);

    if (searchResponse == null) {
      event.getHook().sendMessage(NO_RESULTS_FOUND).queue();
      return;
    }

    LBContributor contributor = searchResponse.items.get(0).contributor;
    ArrayList<MessageEmbed> messageEmbedList = this.contributorEmbedFactory.create(contributor);
    event.getHook().sendMessageEmbeds(messageEmbedList).queue();
  }
}
*/
