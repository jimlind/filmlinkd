package jimlind.filmlinkd.google.pubsub;

import com.google.cloud.pubsub.v1.Subscriber;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

/** Wrapper for the Subscriber.Listener that logs what the subscribers are doing. */
@Singleton
@Slf4j
public class SubscriberListener extends Subscriber.Listener {

  @Override
  public void failed(Subscriber.State from, Throwable failure) {
    if (log.isInfoEnabled()) {
      log.info("The Pub/Sub subscriber has encountered a fatal error and is shutting down.");
      log.info(failure.toString());
    }
    throw new IllegalStateException("PubSubListener Failed");
  }

  @Override
  public void running() {
    log.info("The Pub/Sub subscriber has successfully started and is running.");
  }

  @Override
  public void starting() {
    log.info("The Pub/Sub subscriber is starting.");
  }

  @Override
  public void stopping(Subscriber.State from) {
    log.info("The Pub/Sub subscriber is stopping.");
  }

  @Override
  public void terminated(Subscriber.State from) {
    log.info("The Pub/Sub subscriber has been terminated.");
    throw new IllegalStateException("PubSubListener Terminated");
  }
}
