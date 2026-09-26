package jimlind.filmlinkd.database.channel;

/** Values stored in the user_channels table for a single channel subscription. */
public record UserChannelRow(String letterboxdId, long channelId) {}
