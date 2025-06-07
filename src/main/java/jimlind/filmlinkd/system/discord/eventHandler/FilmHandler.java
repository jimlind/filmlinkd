package jimlind.filmlinkd.system.discord.eventHandler;
/*
import java.util.ArrayList;
import jimlind.filmlinkd.factory.messageEmbed.FilmEmbedFactory;
import jimlind.filmlinkd.model.CombinedLBFilmModel;
import jimlind.filmlinkd.system.letterboxd.api.FilmAPI;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FilmHandler implements Handler {
  @Autowired private FilmAPI filmAPI;
  @Autowired private FilmEmbedFactory filmEmbedFactory;

  public String getEventName() {
    return "film";
  }

  public void handleEvent(SlashCommandInteractionEvent event) {
    event.deferReply().queue();

    OptionMapping optionMapping = event.getInteraction().getOption("film-name");
    String filmName = optionMapping != null ? optionMapping.getAsString() : "";
    CombinedLBFilmModel combinedLBFilmModel = this.filmAPI.fetch(filmName);

    if (combinedLBFilmModel == null) {
      event.getHook().sendMessage(NO_RESULTS_FOUND).queue();
      return;
    }

    ArrayList<MessageEmbed> messageEmbedList = this.filmEmbedFactory.create(combinedLBFilmModel);
    event.getHook().sendMessageEmbeds(messageEmbedList).queue();
  }
}
*/
