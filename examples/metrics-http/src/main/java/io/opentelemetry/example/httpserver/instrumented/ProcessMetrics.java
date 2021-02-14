package io.oopentelemetry.example.httpserver.instrumented;

import io.opentelemetry.api.metrics.LongValueObserver;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.api.metrics.common.Labels;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

/**
 * This class defines instruments to pull process (cpu load, e.g) metrics.
 *
 * <p>These are NOT attached to the global metric provider by default, so you can move them where
 * you need.
 */
public class ProcessMetrics {
  private final OperatingSystemMXBean osJmx;
  private final Meter metrics;
  private final LongValueObserver cpuUsage;
  private final Labels labels;

  private ProcessMetrics(Meter metrics) {
    this.metrics = metrics;
    this.osJmx = ManagementFactory.getOperatingSystemMXBean();
    // TODO: This should be labels for CPU metrics.
    this.labels = Labels.empty();
    this.cpuUsage =
        this.metrics
            .longValueObserverBuilder("jvm.cpu.usage")
            .setDescription(
                "the system (user + kernel) CPU utilization in percentage, ranging [0, 100]")
            .setUnit("100%")
            .setUpdater(result -> result.observe(getCpuLoadPercentage(), this.labels))
            .build();
  }

  /** Implementation which pulls CPU usage from JMX extension to JDK. */
  private long getCpuLoadPercentage() {
    return (long) (this.osJmx.getSystemLoadAverage() * 100L);
  }

  /** Creates process-metric readers against the given meter provider. */
  public static ProcessMetrics create(MeterProvider provider) {
    return new ProcessMetrics(
        provider.get("io.opentelemetry.example.metrics-http.process", "0.1.0"));
  }
}
