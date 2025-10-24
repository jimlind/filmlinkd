package jimlind.filmlinkd.config.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import jimlind.filmlinkd.google.secret.DummySecretManager;
import jimlind.filmlinkd.google.secret.SecretManager;
import jimlind.filmlinkd.system.google.firestore.FirestoreProvider;
import jimlind.filmlinkd.system.google.firestore.UserReader;
import jimlind.filmlinkd.system.google.firestore.UserWriter;
import jimlind.filmlinkd.system.google.firestore.VipReader;
import jimlind.filmlinkd.system.google.pubsub.PubSubManager;
import jimlind.filmlinkd.system.google.pubsub.PublisherCreator;
import jimlind.filmlinkd.system.google.pubsub.SubscriberListener;
import jimlind.filmlinkd.system.google.pubsub.SubscriptionCreator;

/** Google modules for dependency injection. */
public class GoogleModule extends AbstractModule {
  public static final String TRACING_MODE = "tracing";

  @Override
  protected void configure() {
    // Configure a Dummy Secret Manager when Tracing
    String mode = System.getProperty("app.mode");
    if (TRACING_MODE.equals(mode)) {
      bind(SecretManager.class).to(DummySecretManager.class).in(Scopes.SINGLETON);
    } else {
      bind(SecretManager.class).in(Scopes.SINGLETON);
    }

    // Google System Modules
    bind(FirestoreProvider.class).in(Scopes.SINGLETON);
    bind(UserReader.class).in(Scopes.SINGLETON);
    bind(UserWriter.class).in(Scopes.SINGLETON);
    bind(VipReader.class).in(Scopes.SINGLETON);

    // Google PubSub Modules
    bind(PublisherCreator.class).in(Scopes.SINGLETON);
    bind(PubSubManager.class).in(Scopes.SINGLETON);
    bind(SubscriberListener.class).in(Scopes.SINGLETON);
    bind(SubscriptionCreator.class).in(Scopes.SINGLETON);
  }
}
