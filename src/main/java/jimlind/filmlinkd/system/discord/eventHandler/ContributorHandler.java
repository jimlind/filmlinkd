package jimlind.filmlinkd.system.discord.eventHandler;

import com.google.inject.Inject;
import java.util.ArrayList;
import jimlind.filmlinkd.system.discord.embedBuilder.ContributorEmbedBuilder;
import jimlind.filmlinkd.system.letterboxd.api.ContributorAPI;
import jimlind.filmlinkd.system.letterboxd.model.LBContributor;
import jimlind.filmlinkd.system.letterboxd.model.LBSearchResponse;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class ContributorHandler implements Handler {

  private final ContributorAPI contributorAPI;
  private final ContributorEmbedBuilder contributorEmbedBuilder;

  @Inject
  ContributorHandler(
      ContributorAPI contributorAPI, ContributorEmbedBuilder contributorEmbedBuilder) {
    this.contributorAPI = contributorAPI;
    this.contributorEmbedBuilder = contributorEmbedBuilder;
  }

  @Override
  public void handleEvent(SlashCommandInteractionEvent event) {
    event.deferReply().queue();

    OptionMapping optionMapping = event.getInteraction().getOption("contributor-name");
    String contributorName = optionMapping != null ? optionMapping.getAsString() : "";
    LBSearchResponse searchResponse = this.contributorAPI.fetch(contributorName);

    if (searchResponse == null) {
      event.getHook().sendMessage(NO_RESULTS_FOUND).queue();
      return;
    }

    LBContributor contributor = searchResponse.items.getFirst().contributor;
    ArrayList<MessageEmbed> messageEmbedList =
        contributorEmbedBuilder.setContributor(contributor).build();
    event.getHook().sendMessageEmbeds(messageEmbedList).queue();
  }
}
