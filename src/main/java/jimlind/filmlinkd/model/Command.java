package jimlind.filmlinkd.model;

import com.google.gson.Gson;

/** Command Model used to inform all shards and applications about current state. */
public class Command {
  public Type type = Type.FOLLOW;
  public String user;
  public String entry;

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
