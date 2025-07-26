package jimlind.filmlinkd.runnable;

import com.google.inject.Inject;
import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadMXBean;
import jimlind.filmlinkd.config.AppConfig;
import lombok.extern.slf4j.Slf4j;

/**
 * Log a bunch of information about the status of the JVM as info for debugging purposes. There are
 * lots of ways that you could configure different systems to access this data but this feels the
 * most predictable for me.
 */
@Slf4j
@SuppressWarnings({"PMD.AvoidDuplicateLiterals", "PMD.LawOfDemeter"})
public class StatLogger implements Runnable {
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
  public StatLogger(AppConfig appConfig) {
    this.appConfig = appConfig;
    classLoadingBean = ManagementFactory.getClassLoadingMXBean();
    memoryBean = ManagementFactory.getMemoryMXBean();
    threadBean = ManagementFactory.getThreadMXBean();
  }

  @Override
  public void run() {
    if (!log.isInfoEnabled()) {
      return;
    }

    MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
    log.atInfo()
        .setMessage("JVM Heap Statistics (MB)")
        .addKeyValue("used", (double) heapUsage.getUsed() / MEGABYTE)
        .addKeyValue("committed", (double) heapUsage.getCommitted() / MEGABYTE)
        .addKeyValue("max", (double) heapUsage.getMax() / MEGABYTE)
        .addKeyValue("mainClass", appConfig.getMainClass())
        .log();

    MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();
    log.atInfo()
        .setMessage("JVM Non-Heap Statistics (MB)")
        .addKeyValue("used", (double) nonHeapUsage.getUsed() / MEGABYTE)
        .addKeyValue("committed", (double) nonHeapUsage.getCommitted() / MEGABYTE)
        .addKeyValue("max", (double) nonHeapUsage.getMax() / MEGABYTE)
        .addKeyValue("mainClass", appConfig.getMainClass())
        .log();

    log.atInfo()
        .setMessage("JVM Thread Count")
        .addKeyValue("live", threadBean.getThreadCount())
        .addKeyValue("peak", threadBean.getPeakThreadCount())
        .addKeyValue("daemon", threadBean.getDaemonThreadCount())
        .addKeyValue("total-started", threadBean.getTotalStartedThreadCount())
        .addKeyValue("mainClass", appConfig.getMainClass())
        .log();

    log.atInfo()
        .setMessage("JVM Class Count")
        .addKeyValue("live", classLoadingBean.getLoadedClassCount())
        .addKeyValue("total-loaded", classLoadingBean.getTotalLoadedClassCount())
        .addKeyValue("unloaded", classLoadingBean.getUnloadedClassCount())
        .addKeyValue("mainClass", appConfig.getMainClass())
        .log();
  }
}
