package jimlind.filmlinkd.runnable;

import com.google.inject.Inject;
import java.lang.management.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StatLogger implements Runnable {
  private static final long MEGABYTE = 1024L * 1024L;

  private final ClassLoadingMXBean classLoadingBean;
  private final MemoryMXBean memoryBean;
  private final ThreadMXBean threadBean;

  private final NumberFormat formatter;

  @Inject
  public StatLogger() {
    classLoadingBean = ManagementFactory.getClassLoadingMXBean();
    memoryBean = ManagementFactory.getMemoryMXBean();
    threadBean = ManagementFactory.getThreadMXBean();

    formatter = new DecimalFormat("#0.00");
  }

  @Override
  public void run() {
    if (!log.isInfoEnabled()) {
      return;
    }

    MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
    log.info(
        "{memory-heap-used-mb:{}, memory-heap-committed-mb:{}, memory-heap-max-mb={}}",
        formatter.format(heapUsage.getUsed() / MEGABYTE),
        formatter.format(heapUsage.getCommitted() / MEGABYTE),
        formatter.format(heapUsage.getMax() / MEGABYTE));

    MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();
    log.info(
        "{memory-non-heap-used-mb:{}, memory-non-heap-committed-mb:{}, memory-non-heap-max-mb={}}",
        formatter.format(nonHeapUsage.getUsed() / MEGABYTE),
        formatter.format(nonHeapUsage.getCommitted() / MEGABYTE),
        formatter.format(nonHeapUsage.getMax() / MEGABYTE));

    log.info(
        "{thread-live-qty:{}, thread-peak-qty:{}, thread-daemon-qty:{}, thread-started-qty:{}}",
        threadBean.getThreadCount(),
        threadBean.getPeakThreadCount(),
        threadBean.getDaemonThreadCount(),
        threadBean.getTotalStartedThreadCount());

    log.info(
        "{current-loaded-class-qty:{}, total-loaded-class-qty:{}, total-unloaded-class-qty:{}}",
        classLoadingBean.getLoadedClassCount(),
        classLoadingBean.getTotalLoadedClassCount(),
        classLoadingBean.getUnloadedClassCount());
  }
}
