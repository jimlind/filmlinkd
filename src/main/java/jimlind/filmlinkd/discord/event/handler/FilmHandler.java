package jimlind.filmlinkd.discord.event.handler;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import jimlind.filmlinkd.discord.embed.factory.FilmEmbedFactory;
import jimlind.filmlinkd.model.CombinedLbFilmModel;
import jimlind.filmlinkd.system.letterboxd.api.FilmApi;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

/** Handles the /film command to show a film matching the search input. */
@Singleton
public class FilmHandler implements Handler {

  private final FilmApi filmApi;
  private final FilmEmbedFactory filmEmbedFactory;

  /**
   * Constructor for this class.
   *
   * @param filmApi Fetches film data from Letterboxd API
   * @param filmEmbedFactory Builds the embed for the /film command
   */
  @Inject
  FilmHandler(FilmApi filmApi, FilmEmbedFactory filmEmbedFactory) {
    this.filmApi = filmApi;
    this.filmEmbedFactory = filmEmbedFactory;
  }

  @Override
  public void handleEvent(SlashCommandInteractionEvent event) {
    event.deferReply().queue();

    OptionMapping optionMapping = event.getInteraction().getOption("film-name");
    String filmName = optionMapping != null ? optionMapping.getAsString() : "";
    CombinedLbFilmModel combinedLbFilmModel = filmApi.fetch(filmName);

    if (combinedLbFilmModel == null) {
      event.getHook().sendMessage(NO_RESULTS_FOUND).queue();
      return;
    }

    List<MessageEmbed> messageEmbedList = filmEmbedFactory.create(combinedLbFilmModel);
    event.getHook().sendMessageEmbeds(messageEmbedList).queue();
  }
}
