package jimlind.filmlinkd.system.discord.eventhandler;

import com.google.inject.Inject;
import java.util.List;
import jimlind.filmlinkd.discord.factory.ContributorEmbedFactory;
import jimlind.filmlinkd.system.letterboxd.api.ContributorApi;
import jimlind.filmlinkd.system.letterboxd.model.LbContributor;
import jimlind.filmlinkd.system.letterboxd.model.LbSearchResponse;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

/** Handles the /contributor command to show the contributors matching the search input. */
public class ContributorHandler implements Handler {

  private final ContributorApi contributorApi;
  private final ContributorEmbedFactory contributorEmbedFactory;

  /**
   * Constructor for this class.
   *
   * @param contributorApi Fetches contributor data from Letterboxd API
   * @param contributorEmbedFactory Builds the embed for the /contributor command
   */
  @Inject
  ContributorHandler(
      ContributorApi contributorApi, ContributorEmbedFactory contributorEmbedFactory) {
    this.contributorApi = contributorApi;
    this.contributorEmbedFactory = contributorEmbedFactory;
  }

  @Override
  public void handleEvent(SlashCommandInteractionEvent event) {
    event.deferReply().queue();

    OptionMapping optionMapping = event.getInteraction().getOption("contributor-name");
    String contributorName = optionMapping != null ? optionMapping.getAsString() : "";
    LbSearchResponse searchResponse = this.contributorApi.fetch(contributorName);

    if (searchResponse == null) {
      event.getHook().sendMessage(NO_RESULTS_FOUND).queue();
      return;
    }

    LbContributor contributor = searchResponse.getItems().getFirst().contributor;
    List<MessageEmbed> messageEmbedList =
        contributorEmbedFactory.setContributor(contributor).build();
    event.getHook().sendMessageEmbeds(messageEmbedList).queue();
  }
}
