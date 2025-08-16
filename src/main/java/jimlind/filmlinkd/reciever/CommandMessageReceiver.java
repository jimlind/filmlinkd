package jimlind.filmlinkd.reciever;

import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.pubsub.v1.PubsubMessage;
import lombok.extern.slf4j.Slf4j;

/** The CommandMessageReceiver gets PubSub messages and responds to them appropriately. */
@Slf4j
@Singleton
public class CommandMessageReceiver implements com.google.cloud.pubsub.v1.MessageReceiver {

  /** Constructor for the {@link CommandMessageReceiver}. */
  @Inject
  public CommandMessageReceiver() {}

  /**
   * Receives the command message.
   *
   * @param pubsubMessage The message coming from PubSub event containing the payload
   * @param ackReplyConsumer The class needed to acknowledge receiving the message
   */
  @Override
  public void receiveMessage(PubsubMessage pubsubMessage, AckReplyConsumer ackReplyConsumer) {
    String messagePayload = pubsubMessage.getData().toStringUtf8();
    log.atInfo().setMessage(messagePayload).log();

    ackReplyConsumer.ack();
  }
}
