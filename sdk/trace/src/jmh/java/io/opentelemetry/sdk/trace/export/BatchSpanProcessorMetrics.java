/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.export;

import static io.opentelemetry.api.common.AttributeKey.booleanKey;

import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import java.util.Collection;
import java.util.OptionalLong;
import org.openjdk.jmh.annotations.AuxCounters;

public class BatchSpanProcessorMetrics {
  private final Collection<MetricData> allMetrics;
  private final int numThreads;

  public BatchSpanProcessorMetrics(Collection<MetricData> allMetrics, int numThreads) {
    this.allMetrics = allMetrics;
    this.numThreads = numThreads;
  }

  /**
   * Returns the share of dropped spans as a value in {@code [0, 1]}.
   *
   * <p>Only meaningful for benchmarks whose {@link AuxCounters} use {@link
   * AuxCounters.Type#EVENTS}: JMH emits a {@code ScalarResult} with {@code AggregationPolicy.SUM},
   * which sums the aux counters across the {@code numThreads} thread-local {@code @State} instances
   * and the {@code numIterations} measurement iterations. The raw {@code dropped / (exported +
   * dropped)} is therefore inflated by {@code numThreads * numIterations}, and both factors are
   * divided back out here so the value reported by JMH equals the per-iteration drop ratio.
   *
   * <p>For {@link AuxCounters.Type#OPERATIONS} JMH emits a {@code ThroughputResult} (rate per unit
   * time) aggregated as a mean across iterations, so this method does not apply — the drop rate is
   * read directly from {@link #droppedSpans()} and the ratio, if needed, is computed from the two
   * rates.
   */
  public double dropRatio(int numIterations) {
    long exported = getMetric(false);
    long dropped = getMetric(true);
    long total = exported + dropped;
    return total == 0 ? 0.0 : (double) dropped / total / numThreads / numIterations;
  }

  public long exportedSpans() {
    return getMetric(false) / numThreads;
  }

  public long droppedSpans() {
    return getMetric(true) / numThreads;
  }

  private long getMetric(boolean dropped) {
    OptionalLong value =
        allMetrics.stream()
            .filter(metricData -> metricData.getName().equals("processedSpans"))
            .filter(metricData -> !metricData.isEmpty())
            .map(metricData -> metricData.getLongSumData().getPoints())
            .flatMap(Collection::stream)
            .filter(
                point -> {
                  Boolean attrDropped = point.getAttributes().get(booleanKey("dropped"));
                  return attrDropped != null && attrDropped == dropped;
                })
            .mapToLong(LongPointData::getValue)
            .findFirst();
    return value.isPresent() ? value.getAsLong() : 0;
  }
}
