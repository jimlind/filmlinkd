package jimlind.filmlinkd.google.pubsub;

import jimlind.filmlinkd.model.Command;
import jimlind.filmlinkd.model.Message;

public interface PubSubManagerInterface {

  /** Builds all the needed publishers and subscribers. */
  void buildAll();

  /** Builds the command publisher. */
  void buildCommandPublisher();

  /** Builds the log entry publisher. */
  void buildLogEntryPublisher();

  /** Builds the command subscriber. */
  void buildCommandSubscriber();

  /** Builds the log entry subscriber. */
  void buildLogEntrySubscriber();

  /** Deactivates the publishers and subscribers if they have been created. */
  void deactivateAll();

  /**
   * Publishes a command to the command topic. Commands capture when users have performed a slash
   * command so all shards can react in the same way.
   *
   * @param command The command to translate to JSON then publish
   */
  void publishCommand(Command command);

  /**
   * Publishes a log entry to the log entry topic. LogEntry captures when new entries are found
   * (usually by scraping) so that all shards can have a consistent state.
   *
   * @param logEntry The log entry to translate to JSON then publish
   */
  void publishLogEntry(Message logEntry);
}
