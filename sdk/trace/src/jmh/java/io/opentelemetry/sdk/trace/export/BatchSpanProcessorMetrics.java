/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.export;

import static io.opentelemetry.api.common.AttributeKey.booleanKey;

import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import java.util.Collection;

public class BatchSpanProcessorMetrics {
  private final Collection<MetricData> allMetrics;

  public BatchSpanProcessorMetrics(Collection<MetricData> allMetrics) {
    this.allMetrics = allMetrics;
  }

  public long exportedSpans() {
    return getMetric(false);
  }

  public long droppedSpans() {
    return getMetric(true);
  }

  private long getMetric(boolean dropped) {
    return allMetrics.stream()
        .filter(metricData -> metricData.getName().equals("processedSpans"))
        .map(metricData -> metricData.getLongSumData().getPoints())
        .flatMap(Collection::stream)
        .filter(point -> {
          Boolean attrDropped = point.getAttributes().get(booleanKey("dropped"));
          return attrDropped != null && attrDropped == dropped;
        })
        .mapToLong(LongPointData::getValue)
        .sum();
  }
}
