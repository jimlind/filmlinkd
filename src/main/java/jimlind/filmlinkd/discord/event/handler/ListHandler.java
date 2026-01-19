package jimlind.filmlinkd.discord.event.handler;

import java.util.List;
import java.util.Locale;
import javax.inject.Inject;
import javax.inject.Singleton;
import jimlind.filmlinkd.discord.embed.factory.ListEmbedFactory;
import jimlind.filmlinkd.system.discord.helper.AccountHelper;
import jimlind.filmlinkd.system.letterboxd.api.ListApi;
import jimlind.filmlinkd.system.letterboxd.model.LbListSummary;
import jimlind.filmlinkd.system.letterboxd.model.LbListsResponse;
import jimlind.filmlinkd.system.letterboxd.model.LbMember;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

/** Handles the /list command to a summary of a user's list matching the search input. */
@Singleton
public class ListHandler implements Handler {

  private final AccountHelper accountHelper;
  private final ListApi listApi;
  private final ListEmbedFactory listEmbedFactory;

  /**
   * Constructor for this class.
   *
   * @param accountHelper Handles translating account names to proper data models
   * @param listApi Fetches list data from Letterboxd API
   * @param listEmbedFactory Builds the embed for the /list command
   */
  @Inject
  ListHandler(AccountHelper accountHelper, ListApi listApi, ListEmbedFactory listEmbedFactory) {
    this.accountHelper = accountHelper;
    this.listApi = listApi;
    this.listEmbedFactory = listEmbedFactory;
  }

  @Override
  public void handleEvent(SlashCommandInteractionEvent event) {
    event.deferReply().queue();

    LbMember member = accountHelper.getMember(event);
    if (member == null) {
      event.getHook().sendMessage(NO_RESULTS_FOUND).queue();
      return;
    }

    OptionMapping listOptionMapping = event.getInteraction().getOption("list-name");
    String listNameString = listOptionMapping != null ? listOptionMapping.getAsString() : "";

    String cleanListName = listNameString.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "");

    LbListSummary foundList = null;
    LbListsResponse listsResponse = listApi.fetch(member.id, 50);
    if (listsResponse == null) {
      event.getHook().sendMessage("No Results Found").queue();
      return;
    }

    for (LbListSummary item : listsResponse.getItems()) {
      if (cleanListName.equals(
          item.getName().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", ""))) {
        foundList = item;
      }
    }

    if (foundList == null) {
      event.getHook().sendMessage("No Results Found").queue();
      return;
    }

    List<MessageEmbed> messageEmbedList = listEmbedFactory.create(foundList);
    event.getHook().sendMessageEmbeds(messageEmbedList).queue();
  }
}
