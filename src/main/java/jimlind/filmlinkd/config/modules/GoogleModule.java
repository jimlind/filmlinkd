package jimlind.filmlinkd.config.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import jimlind.filmlinkd.system.google.SecretManager;
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
  @Override
  protected void configure() {
    // Google System Modules
    bind(SecretManager.class).in(Scopes.SINGLETON);
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
