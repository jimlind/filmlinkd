package jimlind.filmlinkd.google.secret;

import com.google.cloud.secretmanager.v1.AccessSecretVersionRequest;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

/**
 * Handles all things related to the Secret service. Currently only getting because I set the
 * secrets myself using other methods.
 */
@Singleton
@Slf4j
public class SecretManager {
  private @Nullable SecretManagerServiceClient client;

  /** The constructor for this class. */
  @Inject
  public SecretManager() {
    try {
      this.client = SecretManagerServiceClient.create();
    } catch (IOException e) {
      log.error("Failed to create SecretManagerServiceClient", e);
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
    if (client == null) {
      log.error("Attempting to fetch a secret while SecretManagerServiceClient is null");
      return String.format("dummy-secret-for-%s-in-%s", secretName, projectId);
    }

    String secretPath =
        String.format("projects/%s/secrets/%s/versions/latest", projectId, secretName);
    AccessSecretVersionRequest request =
        AccessSecretVersionRequest.newBuilder().setName(secretPath).build();
    return client.accessSecretVersion(request).getPayload().getData().toStringUtf8();
  }
}
