package jimlind.filmlinkd.system.discord.embedBuilder;

import com.google.inject.Inject;
import io.github.furstenheim.CopyDown;
import io.github.furstenheim.Options;
import io.github.furstenheim.OptionsBuilder;
import java.util.ArrayList;
import jimlind.filmlinkd.factory.EmbedBuilderFactory;
import jimlind.filmlinkd.system.discord.stringBuilder.DescriptionStringBuilder;
import jimlind.filmlinkd.system.letterboxd.model.LBListEntrySummary;
import jimlind.filmlinkd.system.letterboxd.model.LBListSummary;
import jimlind.filmlinkd.system.letterboxd.utils.ImageUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class ListEmbedBuilder {
  private final EmbedBuilder embedBuilder;
  private LBListSummary listSummary = null;

  @Inject
  ListEmbedBuilder(EmbedBuilderFactory embedBuilderFactory) {
    embedBuilder = embedBuilderFactory.create();
  }

  public ListEmbedBuilder setListSummary(LBListSummary listSummary) {
    this.listSummary = listSummary;
    return this;
  }

  public ArrayList<MessageEmbed> build() {
    if (listSummary == null) {
      return new ArrayList<>();
    }

    embedBuilder.setTitle(listSummary.name);
    embedBuilder.setUrl(String.format("https://boxd.it/%s", listSummary.id));
    embedBuilder.setThumbnail(ImageUtils.getTallest(listSummary.previewEntries.get(0).film.poster));

    StringBuilder descriptionText =
        new StringBuilder(
            String.format(
                "**List of %s films curated by [%s](https://boxd.it/%s)**\n\n",
                listSummary.filmCount, listSummary.owner.displayName, listSummary.owner.id));

    Options options = OptionsBuilder.anOptions().withBr("\n").build();
    String listDescription = new CopyDown(options).convert(listSummary.description);
    descriptionText.append(listDescription.replaceAll("[\r\n]+", "\n")).append("\n");

    for (LBListEntrySummary summary : listSummary.previewEntries) {
      String prefix = summary.rank != 0 ? "1." : "-";
      descriptionText.append(
          String.format(
              "%s [%s (%s)](https://boxd.it/%s)\n",
              prefix, summary.film.name, summary.film.releaseYear, summary.film.id));
    }

    String description =
        new DescriptionStringBuilder().setDescriptionText(descriptionText.toString()).build();
    embedBuilder.setDescription(description);

    ArrayList<MessageEmbed> embedList = new ArrayList<>();
    embedList.add(embedBuilder.build());

    return embedList;
  }
}
