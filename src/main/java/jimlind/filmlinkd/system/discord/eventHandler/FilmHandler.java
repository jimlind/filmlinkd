package jimlind.filmlinkd.system.discord.eventHandler;

import com.google.inject.Inject;
import java.util.ArrayList;
import jimlind.filmlinkd.model.CombinedLBFilmModel;
import jimlind.filmlinkd.system.discord.embedBuilder.FilmEmbedBuilder;
import jimlind.filmlinkd.system.letterboxd.api.FilmAPI;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class FilmHandler implements Handler {

  private final FilmAPI filmAPI;
  private final FilmEmbedBuilder filmEmbedBuilder;

  @Inject
  FilmHandler(FilmAPI filmAPI, FilmEmbedBuilder filmEmbedBuilder) {
    this.filmAPI = filmAPI;
    this.filmEmbedBuilder = filmEmbedBuilder;
  }

  @Override
  public void handleEvent(SlashCommandInteractionEvent event) {
    event.deferReply().queue();

    OptionMapping optionMapping = event.getInteraction().getOption("film-name");
    String filmName = optionMapping != null ? optionMapping.getAsString() : "";
    CombinedLBFilmModel combinedLBFilmModel = filmAPI.fetch(filmName);

    if (combinedLBFilmModel == null) {
      event.getHook().sendMessage(NO_RESULTS_FOUND).queue();
      return;
    }

    ArrayList<MessageEmbed> messageEmbedList = filmEmbedBuilder.build(combinedLBFilmModel);
    event.getHook().sendMessageEmbeds(messageEmbedList).queue();
  }
}
