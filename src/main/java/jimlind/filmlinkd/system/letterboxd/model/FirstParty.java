package jimlind.filmlinkd.system.letterboxd.model;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface FirstParty {
  // This only exists so I can easily and visibly document the LetterBoxd data properties that
  // aren't accessible
}
