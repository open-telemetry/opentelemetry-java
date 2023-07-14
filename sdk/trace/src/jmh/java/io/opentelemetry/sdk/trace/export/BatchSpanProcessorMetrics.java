/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.export;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static io.opentelemetry.sdk.internal.AttributeValueConstants.PROCESS_STATUS_DROPPED;
import static io.opentelemetry.sdk.internal.AttributeValueConstants.PROCESS_STATUS_EXPORTED;
import static io.opentelemetry.sdk.internal.AttributeValueConstants.PROCESS_STATUS_PROCESSED;

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
    long dropped = getMetric(PROCESS_STATUS_DROPPED);
    long total = getMetric(PROCESS_STATUS_PROCESSED);
    // Due to peculiarities of JMH reporting we have to divide this by the number of the
    // concurrent threads running the actual benchmark.
    return total == 0 ? 0 : (double) dropped / total / numThreads;
  }

  public long exportedSpans() {
    return getMetric(PROCESS_STATUS_EXPORTED) / numThreads;
  }

  public long droppedSpans() {
    return getMetric(PROCESS_STATUS_DROPPED) / numThreads;
  }

  private long getMetric(String status) {
    String labelValue = status;
    OptionalLong value =
        allMetrics.stream()
            .filter(metricData -> metricData.getName().equals("processedSpans"))
            .filter(metricData -> !metricData.isEmpty())
            .map(metricData -> metricData.getLongSumData().getPoints())
            .flatMap(Collection::stream)
            .filter(point -> labelValue.equals(point.getAttributes().get(stringKey("status"))))
            .mapToLong(LongPointData::getValue)
            .findFirst();
    return value.isPresent() ? value.getAsLong() : 0;
  }
}
