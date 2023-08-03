/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opencensusshim.metrics;

import io.opencensus.metrics.Metrics;
import io.opencensus.metrics.export.MetricProducerManager;
import io.opentelemetry.opencensusshim.internal.metrics.MetricAdapter;
import io.opentelemetry.sdk.metrics.data.ScopeMetricData;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.metrics.internal.export.MetricProducer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A producer instance of OpenCensus metrics.
 *
 * <p>We can provide the OpenCensus {@link MetricProducer} when creating a
 * {@link MetricReader}, allowing the reader to combine
 * metrics from both OpenTelemetry and OpenCensus instrumentation.
 */
final class OpenCensusMetricProducer implements MetricProducer {
  private final MetricProducerManager openCensusMetricStorage;

  OpenCensusMetricProducer(MetricProducerManager openCensusMetricStorage) {
    this.openCensusMetricStorage = openCensusMetricStorage;
  }

  /**
   * Constructs a new {@link OpenCensusMetricProducer}.
   */
  static MetricProducer create() {
    return new OpenCensusMetricProducer(Metrics.getExportComponent().getMetricProducerManager());
  }

  @Override
  public Collection<ScopeMetricData> collectAllMetrics() {
    List<ScopeMetricData> result = new ArrayList<>();
    openCensusMetricStorage
        .getAllMetricProducer()
        .forEach(
            producer -> {
              producer.getMetrics().forEach(metric -> result.add(MetricAdapter.convert(metric)));
            });
    return result;
  }
}
