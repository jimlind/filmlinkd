package jimlind.filmlinkd.system.google;

import com.google.cloud.secretmanager.v1.AccessSecretVersionRequest;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;

/**
 * Handles all things related to the Secret service. Currently only getting because I set the
 * secrets myself using other methods.
 */
@Singleton
public class SecretManager {
  private final SecretManagerServiceClient client;

  /** The constructor for this class. */
  @Inject
  SecretManager() {
    // TODO: Check what sort of exception I can actually get out of here.
    try {
      this.client = SecretManagerServiceClient.create();
    } catch (IOException e) {
      throw new IllegalStateException("Failed to create SecretManagerServiceClient", e);
    }
  }

  /**
   * Gets the secret value as string based on necessary inputs.
   *
   * @param projectId The string project name needed to access the secret
   * @param secretName The string name of the secret
   * @return The string value of the secret
   */
  public String getSecret(String projectId, String secretName) {
    String secretPath =
        String.format("projects/%s/secrets/%s/versions/latest", projectId, secretName);
    AccessSecretVersionRequest request =
        AccessSecretVersionRequest.newBuilder().setName(secretPath).build();
    return client.accessSecretVersion(request).getPayload().getData().toStringUtf8();
  }
}
