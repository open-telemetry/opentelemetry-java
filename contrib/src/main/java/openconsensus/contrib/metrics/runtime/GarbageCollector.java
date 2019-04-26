package openconsensus.contrib.metrics.runtime;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.Collections;
import java.util.List;
import openconsensus.metrics.LabelKey;
import openconsensus.metrics.LabelValue;
import openconsensus.metrics.LongCumulative;
import openconsensus.metrics.MetricRegistry;
import openconsensus.metrics.Metrics;

/**
 * Exports metrics about JVM garbage collectors.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * new GarbageCollector().exportAll();
 * }</pre>
 *
 * Example metrics being exported:
 *
 * <pre>
 *   jvm_gc_collection{gc="PS1"} 6.7
 * </pre>
 */
public final class GarbageCollector {
  private static final LabelKey GC = LabelKey.create("gc", "");

  private final List<GarbageCollectorMXBean> garbageCollectors;
  private final MetricRegistry metricRegistry;

  public GarbageCollector() {
    this.garbageCollectors = ManagementFactory.getGarbageCollectorMXBeans();
    this.metricRegistry = Metrics.getMeter().metricRegistryBuilder().setComponent("jvm_gc").build();
  }

  public void exportAll() {
    // TODO: This should probably be a cumulative Histogram without buckets (or Summary without
    //  percentiles) to allow count/sum.
    final LongCumulative collectionMetric =
        metricRegistry
            .longCumulativeBuilder("collection")
            .setDescription("Time spent in a given JVM garbage collector in milliseconds.")
            .setUnit("ms")
            .setLabelKeys(Collections.singletonList(GC))
            .build();
    collectionMetric.setCallback(
        new Runnable() {
          @Override
          public void run() {
            for (final GarbageCollectorMXBean gc : garbageCollectors) {
              LabelValue gcName = LabelValue.create(gc.getName());
              collectionMetric
                  .getOrCreateTimeSeries(Collections.singletonList(gcName))
                  .set(gc.getCollectionTime());
            }
          }
        });
  }
}
