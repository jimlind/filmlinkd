package jimlind.filmlinkd.google.secret;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class DummySecretManager extends SecretManager {

  @Inject
  public DummySecretManager() {
    super();
  }

  @Override
  public String getSecret(String projectId, String secretName) {
    return String.format("dummy-secret-for-%s-in-%s", secretName, projectId);
  }
}
