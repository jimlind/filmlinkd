package jimlind.filmlinkd.model;

import com.google.gson.Gson;
import lombok.Getter;

/** Command Model used to inform all shards and applications about current state. */
@Getter
public class Command {
  private final Type type;
  private final String user;
  private final String entry;

  public Command(Type type, String user, String entry) {
    this.type = type;
    this.user = user;
    this.entry = entry;
  }

  /**
   * Get the model as a JSON String. Mostly for easy transit as a PubSub message.
   *
   * @return A JSON String
   */
  public String toJson() {
    return new Gson().toJson(this);
  }

  /** Currently only supported types of commands are FOLLOW. */
  public enum Type {
    FOLLOW,
  }
}
