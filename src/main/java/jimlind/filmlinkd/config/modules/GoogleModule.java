package jimlind.filmlinkd.config.modules;

import com.google.cloud.firestore.Firestore;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import jimlind.filmlinkd.google.db.FirestoreProvider;
import jimlind.filmlinkd.google.db.UserReader;
import jimlind.filmlinkd.google.db.UserWriter;
import jimlind.filmlinkd.google.db.VipReader;
import jimlind.filmlinkd.google.pubsub.PubSubManager;
import jimlind.filmlinkd.google.pubsub.PublisherCreator;
import jimlind.filmlinkd.google.pubsub.SubscriberListener;
import jimlind.filmlinkd.google.pubsub.SubscriptionCreator;
import jimlind.filmlinkd.google.secret.SecretManager;

/** Google modules for dependency injection. */
public class GoogleModule extends AbstractModule {
  @Override
  protected void configure() {
    // Google Database Modules
    bind(Firestore.class).toProvider(FirestoreProvider.class).in(Scopes.SINGLETON);
    bind(UserReader.class).in(Scopes.SINGLETON);
    bind(UserWriter.class).in(Scopes.SINGLETON);
    bind(VipReader.class).in(Scopes.SINGLETON);

    // Google PubSub Modules
    bind(PubSubManager.class).in(Scopes.SINGLETON);
    bind(PublisherCreator.class).in(Scopes.SINGLETON);
    bind(SubscriberListener.class).in(Scopes.SINGLETON);
    bind(SubscriptionCreator.class).in(Scopes.SINGLETON);

    // Google Secrets
    bind(SecretManager.class).in(Scopes.SINGLETON);
  }
}
