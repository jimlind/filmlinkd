package jimlind.filmlinkd.system;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadMXBean;
import javax.inject.Inject;
import javax.inject.Singleton;
import jimlind.filmlinkd.config.AppConfig;
import lombok.extern.slf4j.Slf4j;

/**
 * Log a bunch of information about the status of the JVM as info for debugging purposes. There are
 * lots of ways that you could configure different systems to access this data but this feels the
 * most predictable for me.
 */
@Singleton
@Slf4j
public class MemoryInformationLogger {
  private static final String TOTAL_LOADED_KEY = "total-loaded";
  private static final String USED_KEY = "used";
  private static final String COMMITTED_KEY = "committed";
  private static final String MAX_KEY = "max";
  private static final String MAIN_CLASS_KEY = "mainClass";
  private static final String LIVE_KEY = "live";
  private static final String PEAK_KEY = "peak";
  private static final String DAEMON_KEY = "daemon";
  private static final String TOTAL_STARTED_KEY = "total-started";
  private static final String UNLOADED_KEY = "unloaded";

  private static final long MEGABYTE = 1024L * 1024L;
  private final AppConfig appConfig;
  private final ClassLoadingMXBean classLoadingBean;
  private final MemoryMXBean memoryBean;
  private final ThreadMXBean threadBean;

  /**
   * Constructor for this class.
   *
   * @param appConfig Contains application and environment variables
   */
  @Inject
  public MemoryInformationLogger(AppConfig appConfig) {
    this.appConfig = appConfig;
    classLoadingBean = ManagementFactory.getClassLoadingMXBean();
    memoryBean = ManagementFactory.getMemoryMXBean();
    threadBean = ManagementFactory.getThreadMXBean();
  }

  private static MemoryUsage getNonHeapMemoryUsage(MemoryMXBean memoryBean) {
    return memoryBean.getNonHeapMemoryUsage();
  }

  private static MemoryUsage getHeapMemoryUsage(MemoryMXBean memoryBean) {
    return memoryBean.getHeapMemoryUsage();
  }

  /** Execution path for this logger. Gathers memory information at the moment and logs it. */
  public void run() {
    if (!log.isInfoEnabled()) {
      return;
    }

    MemoryUsage heapUsage = getHeapMemoryUsage(memoryBean);
    log.atInfo()
        .setMessage("JVM Heap Statistics (MB)")
        .addKeyValue(USED_KEY, (double) heapUsage.getUsed() / MEGABYTE)
        .addKeyValue(COMMITTED_KEY, (double) heapUsage.getCommitted() / MEGABYTE)
        .addKeyValue(MAX_KEY, (double) heapUsage.getMax() / MEGABYTE)
        .addKeyValue(MAIN_CLASS_KEY, appConfig.getMainClass())
        .log();

    MemoryUsage nonHeapUsage = getNonHeapMemoryUsage(memoryBean);
    log.atInfo()
        .setMessage("JVM Non-Heap Statistics (MB)")
        .addKeyValue(USED_KEY, (double) nonHeapUsage.getUsed() / MEGABYTE)
        .addKeyValue(COMMITTED_KEY, (double) nonHeapUsage.getCommitted() / MEGABYTE)
        .addKeyValue(MAX_KEY, (double) nonHeapUsage.getMax() / MEGABYTE)
        .addKeyValue(MAIN_CLASS_KEY, appConfig.getMainClass())
        .log();

    log.atInfo()
        .setMessage("JVM Thread Count")
        .addKeyValue(LIVE_KEY, threadBean.getThreadCount())
        .addKeyValue(PEAK_KEY, threadBean.getPeakThreadCount())
        .addKeyValue(DAEMON_KEY, threadBean.getDaemonThreadCount())
        .addKeyValue(TOTAL_STARTED_KEY, threadBean.getTotalStartedThreadCount())
        .addKeyValue(MAIN_CLASS_KEY, appConfig.getMainClass())
        .log();

    log.atInfo()
        .setMessage("JVM Class Count")
        .addKeyValue(LIVE_KEY, classLoadingBean.getLoadedClassCount())
        .addKeyValue(TOTAL_LOADED_KEY, classLoadingBean.getTotalLoadedClassCount())
        .addKeyValue(UNLOADED_KEY, classLoadingBean.getUnloadedClassCount())
        .addKeyValue(MAIN_CLASS_KEY, appConfig.getMainClass())
        .log();
  }
}
