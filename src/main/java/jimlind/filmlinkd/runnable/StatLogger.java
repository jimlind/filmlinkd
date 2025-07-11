package jimlind.filmlinkd.runnable;

import com.google.inject.Inject;
import java.lang.management.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StatLogger implements Runnable {
  private static final long MEGABYTE = 1024L * 1024L;

  private final ClassLoadingMXBean classLoadingBean;
  private final MemoryMXBean memoryBean;
  private final ThreadMXBean threadBean;

  @Inject
  public StatLogger() {
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
        .log();

    MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();
    log.atInfo()
        .setMessage("JVM Non-Heap Statistics (MB)")
        .addKeyValue("used", (double) nonHeapUsage.getUsed() / MEGABYTE)
        .addKeyValue("committed", (double) nonHeapUsage.getCommitted() / MEGABYTE)
        .addKeyValue("max", (double) nonHeapUsage.getMax() / MEGABYTE)
        .log();

    log.atInfo()
        .setMessage("JVM Thread Count")
        .addKeyValue("live", threadBean.getThreadCount())
        .addKeyValue("peak", threadBean.getPeakThreadCount())
        .addKeyValue("daemon", threadBean.getDaemonThreadCount())
        .addKeyValue("total-started", threadBean.getTotalStartedThreadCount())
        .log();

    log.atInfo()
        .setMessage("JVM Class Count")
        .addKeyValue("live", classLoadingBean.getLoadedClassCount())
        .addKeyValue("total-loaded", classLoadingBean.getTotalLoadedClassCount())
        .addKeyValue("unloaded", classLoadingBean.getUnloadedClassCount())
        .log();
  }
}
