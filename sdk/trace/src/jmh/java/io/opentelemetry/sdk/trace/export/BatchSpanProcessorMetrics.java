/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.export;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import java.util.Collection;
import java.util.OptionalLong;

public class BatchSpanProcessorMetrics {
  private final Collection<MetricData> allMetrics;
  private final int numThreads;

  public BatchSpanProcessorMetrics(Collection<MetricData> allMetrics, int numThreads) {
    this.allMetrics = allMetrics;
    this.numThreads = numThreads;
  }

  public double dropRatio() {
    long exported = getMetric(false);
    long dropped = getMetric(true);
    long total = exported + dropped;
    // Due to peculiarities of JMH reporting we have to divide this by the number of the
    // concurrent threads running the actual benchmark.
    return total == 0 ? 0 : (double) dropped / total / numThreads;
  }

  public long exportedSpans() {
    return getMetric(false) / numThreads;
  }

  public long droppedSpans() {
    return getMetric(true) / numThreads;
  }

  private long getMetric(boolean dropped) {
    String labelValue = String.valueOf(dropped);
    OptionalLong value =
        allMetrics.stream()
            .filter(metricData -> metricData.getName().equals("processedSpans"))
            .filter(metricData -> !metricData.isEmpty())
            .map(metricData -> metricData.getLongSumData().getPoints())
            .flatMap(Collection::stream)
            .filter(point -> labelValue.equals(point.getAttributes().get(stringKey("dropped"))))
            .mapToLong(LongPointData::getValue)
            .findFirst();
    return value.isPresent() ? value.getAsLong() : 0;
  }
}
