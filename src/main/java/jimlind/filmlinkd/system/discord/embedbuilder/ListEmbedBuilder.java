package jimlind.filmlinkd.system.discord.embedbuilder;

import com.google.inject.Inject;
import io.github.furstenheim.CopyDown;
import io.github.furstenheim.Options;
import io.github.furstenheim.OptionsBuilder;
import java.util.ArrayList;
import java.util.List;
import jimlind.filmlinkd.factory.EmbedBuilderFactory;
import jimlind.filmlinkd.system.discord.stringbuilder.DescriptionStringBuilder;
import jimlind.filmlinkd.system.letterboxd.model.LbFilmSummary;
import jimlind.filmlinkd.system.letterboxd.model.LbListEntrySummary;
import jimlind.filmlinkd.system.letterboxd.model.LbListSummary;
import jimlind.filmlinkd.system.letterboxd.model.LbMemberSummary;
import jimlind.filmlinkd.system.letterboxd.utils.ImageUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

/** Builds a Discord embed to display information about users list. */
public class ListEmbedBuilder {
  private final EmbedBuilder embedBuilder;
  private final ImageUtils imageUtils;
  private LbListSummary listSummary;

  /**
   * Constructor for this class.
   *
   * @param embedBuilderFactory A factory for creating instances of the {@link EmbedBuilder} model.
   * @param imageUtils Assists in finding optimal Letterboxd images
   */
  @Inject
  ListEmbedBuilder(EmbedBuilderFactory embedBuilderFactory, ImageUtils imageUtils) {
    embedBuilder = embedBuilderFactory.create();
    this.imageUtils = imageUtils;
  }

  private static LbFilmSummary getFilm(LbListEntrySummary summary) {
    return summary.getFilm();
  }

  /**
   * Setter for the listSummary attribute.
   *
   * @param listSummary ListSummary model from Letterboxd API
   * @return This class for chaining
   */
  public ListEmbedBuilder setListSummary(LbListSummary listSummary) {
    this.listSummary = listSummary;
    return this;
  }

  /**
   * Builds the embed.
   *
   * @return A fully constructed list of embeds that are ready to be sent to users. Here the list
   *     contains only one embed.
   */
  public List<MessageEmbed> build() {
    if (listSummary == null) {
      return new ArrayList<>();
    }

    embedBuilder.setTitle(listSummary.name);
    embedBuilder.setUrl("https://boxd.it/" + listSummary.getId());
    embedBuilder.setThumbnail(
        imageUtils.getTallest(getFilm(listSummary.getPreviewEntries().getFirst()).poster));

    StringBuilder descriptionText =
        new StringBuilder(
            String.format(
                "**List of %s films curated by [%s](https://boxd.it/%s)**\n\n",
                listSummary.filmCount, getOwner().displayName, getOwner().id));

    Options options = OptionsBuilder.anOptions().withBr("\n").build();
    String listDescription = new CopyDown(options).convert(listSummary.description);
    descriptionText.append(listDescription.replaceAll("[\r\n]+", "\n")).append('\n');

    for (LbListEntrySummary summary : listSummary.getPreviewEntries()) {
      String prefix = summary.getRank() != 0 ? "1." : "-";
      descriptionText.append(
          String.format(
              "%s [%s (%s)](https://boxd.it/%s)\n",
              prefix, getFilm(summary).name, getFilm(summary).releaseYear, getFilm(summary).id));
    }

    String description =
        new DescriptionStringBuilder().setDescriptionText(descriptionText.toString()).build();
    embedBuilder.setDescription(description);

    List<MessageEmbed> embedList = new ArrayList<>();
    embedList.add(embedBuilder.build());

    return embedList;
  }

  private LbMemberSummary getOwner() {
    return listSummary.getOwner();
  }
}
