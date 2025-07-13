package jimlind.filmlinkd.system.discord.eventhandler;

import com.google.inject.Inject;
import java.util.ArrayList;
import jimlind.filmlinkd.system.discord.embedbuilder.ListEmbedBuilder;
import jimlind.filmlinkd.system.discord.helper.AccountHelper;
import jimlind.filmlinkd.system.letterboxd.api.ListApi;
import jimlind.filmlinkd.system.letterboxd.model.LbListSummary;
import jimlind.filmlinkd.system.letterboxd.model.LbListsResponse;
import jimlind.filmlinkd.system.letterboxd.model.LbMember;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class ListHandler implements Handler {

  private final AccountHelper accountHelper;
  private final ListApi listApi;
  private final ListEmbedBuilder listEmbedBuilder;

  @Inject
  ListHandler(AccountHelper accountHelper, ListApi listApi, ListEmbedBuilder listEmbedBuilder) {
    this.accountHelper = accountHelper;
    this.listApi = listApi;
    this.listEmbedBuilder = listEmbedBuilder;
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

    String cleanListName = listNameString.toLowerCase().replaceAll("[^a-z0-9]+", "");

    LbListSummary foundList = null;
    LbListsResponse listsResponse = listApi.fetch(member.id, 50);
    if (listsResponse == null) {
      event.getHook().sendMessage("No Results Found").queue();
      return;
    }

    for (LbListSummary item : listsResponse.items) {
      if (cleanListName.equals(item.name.toLowerCase().replaceAll("[^a-z0-9]+", ""))) {
        foundList = item;
      }
    }

    if (foundList == null) {
      event.getHook().sendMessage("No Results Found").queue();
      return;
    }

    ArrayList<MessageEmbed> messageEmbedList = listEmbedBuilder.setListSummary(foundList).build();
    event.getHook().sendMessageEmbeds(messageEmbedList).queue();
  }
}
