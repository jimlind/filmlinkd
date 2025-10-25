package jimlind.filmlinkd.config.modules;

import com.google.cloud.firestore.Firestore;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import jimlind.filmlinkd.google.db.DummyUserReader;
import jimlind.filmlinkd.google.db.DummyVipReader;
import jimlind.filmlinkd.google.db.FirestoreProvider;
import jimlind.filmlinkd.google.db.UserReader;
import jimlind.filmlinkd.google.db.UserReaderInterface;
import jimlind.filmlinkd.google.db.VipReader;
import jimlind.filmlinkd.google.db.VipReaderInterface;
import jimlind.filmlinkd.google.secret.DummySecretManager;
import jimlind.filmlinkd.google.secret.SecretManager;
import jimlind.filmlinkd.google.secret.SecretManagerInterface;
import jimlind.filmlinkd.system.google.firestore.UserWriter;
import jimlind.filmlinkd.system.google.pubsub.PubSubManager;
import jimlind.filmlinkd.system.google.pubsub.PublisherCreator;
import jimlind.filmlinkd.system.google.pubsub.SubscriberListener;
import jimlind.filmlinkd.system.google.pubsub.SubscriptionCreator;

/** Google modules for dependency injection. */
public class GoogleModule extends AbstractModule {
  public static final String TRACING_MODE = "tracing";

  @Override
  protected void configure() {
    // Configure Specific Modules for Online and Offline Use
    if (TRACING_MODE.equals(System.getProperty("app.mode"))) {
      bind(SecretManagerInterface.class).to(DummySecretManager.class).in(Scopes.SINGLETON);
      bind(UserReaderInterface.class).to(DummyUserReader.class).in(Scopes.SINGLETON);
      bind(VipReaderInterface.class).to(DummyVipReader.class).in(Scopes.SINGLETON);
    } else {
      bind(SecretManagerInterface.class).to(SecretManager.class).in(Scopes.SINGLETON);
      bind(UserReaderInterface.class).to(UserReader.class).in(Scopes.SINGLETON);
      bind(VipReaderInterface.class).to(VipReader.class).in(Scopes.SINGLETON);
    }

    // Google Database Modules
    bind(Firestore.class).toProvider(FirestoreProvider.class).in(Scopes.SINGLETON);
    bind(UserWriter.class).in(Scopes.SINGLETON);

    // Google PubSub Modules
    bind(PublisherCreator.class).in(Scopes.SINGLETON);
    bind(PubSubManager.class).in(Scopes.SINGLETON);
    bind(SubscriberListener.class).in(Scopes.SINGLETON);
    bind(SubscriptionCreator.class).in(Scopes.SINGLETON);
  }
}
