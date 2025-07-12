package jimlind.filmlinkd.system.discord.eventhandler;

import com.google.inject.Inject;
import java.util.ArrayList;
import jimlind.filmlinkd.model.CombinedLBFilmModel;
import jimlind.filmlinkd.system.discord.embedBuilder.FilmEmbedBuilder;
import jimlind.filmlinkd.system.letterboxd.api.FilmApi;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class FilmHandler implements Handler {

  private final FilmApi filmApi;
  private final FilmEmbedBuilder filmEmbedBuilder;

  @Inject
  FilmHandler(FilmApi filmApi, FilmEmbedBuilder filmEmbedBuilder) {
    this.filmApi = filmApi;
    this.filmEmbedBuilder = filmEmbedBuilder;
  }

  @Override
  public void handleEvent(SlashCommandInteractionEvent event) {
    event.deferReply().queue();

    OptionMapping optionMapping = event.getInteraction().getOption("film-name");
    String filmName = optionMapping != null ? optionMapping.getAsString() : "";
    CombinedLBFilmModel combinedLBFilmModel = filmApi.fetch(filmName);

    if (combinedLBFilmModel == null) {
      event.getHook().sendMessage(NO_RESULTS_FOUND).queue();
      return;
    }

    ArrayList<MessageEmbed> messageEmbedList =
        filmEmbedBuilder.setFilmCombination(combinedLBFilmModel).build();
    event.getHook().sendMessageEmbeds(messageEmbedList).queue();
  }
}
