package jimlind.filmlinkd.model;

import lombok.Getter;

/** Data model for vip information directly mimicking the Firestore model form. */
@Getter
public class Vip {
  private String id;
  private String group;
  private String channelId;
}
