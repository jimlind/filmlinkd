package jimlind.filmlinkd.system.discord.embedBuilder;

import io.github.furstenheim.CopyDown;
import io.github.furstenheim.Options;
import io.github.furstenheim.OptionsBuilder;
import java.util.ArrayList;
import jimlind.filmlinkd.system.discord.utils.EmbedBuilder;
import jimlind.filmlinkd.system.discord.utils.EmbedDescriptionBuilder;
import jimlind.filmlinkd.system.letterboxd.model.LBListEntrySummary;
import jimlind.filmlinkd.system.letterboxd.model.LBListSummary;
import jimlind.filmlinkd.system.letterboxd.utils.ImageUtils;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class ListEmbedBuilder {
  public ArrayList<MessageEmbed> build(LBListSummary listSummary) {
    EmbedBuilder embedBuilder = new EmbedBuilder();

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

    String description = new EmbedDescriptionBuilder(descriptionText.toString()).build();
    embedBuilder.setDescription(description);

    ArrayList<MessageEmbed> collection = new ArrayList<>();
    collection.add(embedBuilder.build());

    return collection;
  }
}
