package jimlind.filmlinkd.factory;

import java.awt.*;
import net.dv8tion.jda.api.EmbedBuilder;

public class EmbedBuilderFactory {
  public EmbedBuilder create() {
    EmbedBuilder embedBuilder = new EmbedBuilder();
    embedBuilder.setColor(new Color(0xa700bd));

    return embedBuilder;
  }
}
