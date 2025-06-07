package jimlind.filmlinkd.system.google;

import com.google.cloud.secretmanager.v1.AccessSecretVersionRequest;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class SecretManager {
  private final SecretManagerServiceClient client;

  @Inject
  SecretManager() {
    try {
      this.client = SecretManagerServiceClient.create();
    } catch (Exception e) {
      throw new RuntimeException("Failed to create SecretManagerServiceClient", e);
    }
  }

  public String getSecret(String secretName) {
    String projectId = "letterboxd-bot";
    String secretPath =
        String.format("projects/%s/secrets/%s/versions/latest", projectId, secretName);
    AccessSecretVersionRequest request =
        AccessSecretVersionRequest.newBuilder().setName(secretPath).build();
    return client.accessSecretVersion(request).getPayload().getData().toStringUtf8();
  }
}
