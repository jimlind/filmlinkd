package jimlind.filmlinkd.model;

import com.google.gson.Gson;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

/** Message is a bit of a broad term here but this is used to send to all shards. */
@Getter
@Setter
public class Message {
  public Entry entry;
  @Nullable public String channelId;

  /**
   * A channel override means that we want to publish to one specific channel and not publish to all
   * channels that the user is followed in.
   *
   * @return Indicator that a channel was specified for the message
   */
  public boolean hasChannelOverride() {
    return channelId != null && !channelId.isBlank();
  }

  /**
   * Needed to create a string object to be passed around in PubSub messages.
   *
   * @return A JSON string of the data in this model
   */
  public String toJson() {
    return new Gson().toJson(this);
  }

  /** The different kinds of diary entries. */
  public enum Type {
    watch,
    review,
  }

  /** Where the new diary entry comes from. */
  public enum PublishSource {
    Normal,
    VIP,
    Follow,
  }

  /** All the data needed for a diary entry. */
  @Getter
  @Setter
  public static class Entry {
    public String lid;
    public String userName;
    public String userLid;
    public Type type;
    public String link;
    public long publishedDate;
    public String filmTitle;
    public Integer filmYear;
    public long watchedDate;
    public String image;
    public float starCount;
    public Boolean rewatch;
    public Boolean liked;
    public Boolean containsSpoilers;
    public Boolean adult;
    public String review;
    public long updatedDate;
    public PublishSource publishSource;
  }
}
