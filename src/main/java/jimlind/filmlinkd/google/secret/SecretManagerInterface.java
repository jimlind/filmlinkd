package jimlind.filmlinkd.google.secret;

public interface SecretManagerInterface {
  String getSecret(String projectId, String secretName);
}
