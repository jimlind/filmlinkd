package jimlind.filmlinkd.system.discord.eventHandler;

import com.google.inject.Inject;
import java.util.ArrayList;
import jimlind.filmlinkd.system.discord.embedBuilder.ListEmbedBuilder;
import jimlind.filmlinkd.system.discord.helper.AccountHelper;
import jimlind.filmlinkd.system.letterboxd.api.ListAPI;
import jimlind.filmlinkd.system.letterboxd.model.LBListSummary;
import jimlind.filmlinkd.system.letterboxd.model.LBListsResponse;
import jimlind.filmlinkd.system.letterboxd.model.LBMember;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class ListHandler implements Handler {

  private final AccountHelper accountHelper;
  private final ListAPI listAPI;
  private final ListEmbedBuilder listEmbedBuilder;

  @Inject
  ListHandler(AccountHelper accountHelper, ListAPI listAPI, ListEmbedBuilder listEmbedBuilder) {
    this.accountHelper = accountHelper;
    this.listAPI = listAPI;
    this.listEmbedBuilder = listEmbedBuilder;
  }

  @Override
  public void handleEvent(SlashCommandInteractionEvent event) {
    event.deferReply().queue();

    LBMember member = accountHelper.getMember(event);
    if (member == null) {
      event.getHook().sendMessage(NO_RESULTS_FOUND).queue();
      return;
    }

    OptionMapping listOptionMapping = event.getInteraction().getOption("list-name");
    String listNameString = listOptionMapping != null ? listOptionMapping.getAsString() : "";

    String cleanListName = listNameString.toLowerCase().replaceAll("[^a-z0-9]+", "");

    LBListSummary foundList = null;
    LBListsResponse listsResponse = listAPI.fetch(member.id, 50);
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

    ArrayList<MessageEmbed> messageEmbedList = listEmbedBuilder.setListSummary(foundList).build();
    event.getHook().sendMessageEmbeds(messageEmbedList).queue();
  }
}
