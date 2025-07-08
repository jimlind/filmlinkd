package jimlind.filmlinkd.runnable;

import com.google.inject.Inject;
import jimlind.filmlinkd.system.GeneralUserCache;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GeneralUserCacheClearTask implements Runnable {

  private final GeneralUserCache generalUserCache;

  @Inject
  public GeneralUserCacheClearTask(GeneralUserCache generalUserCache) {
    this.generalUserCache = generalUserCache;
  }

  @Override
  public void run() {
    generalUserCache.clear();
  }
}
