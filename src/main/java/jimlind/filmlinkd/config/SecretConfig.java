package jimlind.filmlinkd.config;

import com.google.cloud.secretmanager.v1.AccessSecretVersionRequest;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.inject.Singleton;

@Singleton
public enum SecretConfig {
  INSTANCE;

  private final SecretManagerServiceClient client;

  SecretConfig() {
    try {
      this.client = SecretManagerServiceClient.create();
    } catch (Exception e) {
      throw new RuntimeException("Failed to create SecretManagerServiceClient", e);
    }
  }

  public String getSecret(String secretName, String version) {
    String projectId = "letterboxd-bot";
    String secretPath =
        String.format("projects/%s/secrets/%s/versions/%s", projectId, secretName, version);
    AccessSecretVersionRequest request =
        AccessSecretVersionRequest.newBuilder().setName(secretPath).build();
    return client.accessSecretVersion(request).getPayload().getData().toStringUtf8();
  }
}
