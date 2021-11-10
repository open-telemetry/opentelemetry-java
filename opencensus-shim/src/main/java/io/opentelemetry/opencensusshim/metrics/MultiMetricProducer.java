/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opencensusshim.metrics;

import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricProducer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** Class that wraps multiple metric producers into one. */
final class MultiMetricProducer implements MetricProducer {
  private final Collection<MetricProducer> producers;

  public MultiMetricProducer(Collection<MetricProducer> producers) {
    this.producers = producers;
  }

  @Override
  public Collection<MetricData> collectAllMetrics() {
    List<MetricData> result = new ArrayList<>();
    for (MetricProducer p : producers) {
      result.addAll(p.collectAllMetrics());
    }
    return result;
  }
}
