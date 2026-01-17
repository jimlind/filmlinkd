package jimlind.filmlinkd.factory;

import java.awt.Color;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.dv8tion.jda.api.EmbedBuilder;

/** A factory for creating instances of the {@link EmbedBuilder} model. */
@Singleton
public class EmbedBuilderFactory {
  /** Constructor for this class. */
  @Inject
  EmbedBuilderFactory() {}

  /**
   * Creates an {@link EmbedBuilder} with sane defaults. Right now that only includes a default
   * color that is used for Filmlinkd
   *
   * @return The {@link EmbedBuilder} with new color
   */
  public EmbedBuilder create() {
    EmbedBuilder embedBuilder = new EmbedBuilder();
    embedBuilder.setColor(new Color(0xa700bd));

    return embedBuilder;
  }
}
