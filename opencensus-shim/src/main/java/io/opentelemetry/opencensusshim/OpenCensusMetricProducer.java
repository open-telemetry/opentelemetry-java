/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opencensusshim;

import io.opencensus.metrics.Metrics;
import io.opencensus.metrics.export.MetricProducerManager;
import io.opentelemetry.opencensusshim.internal.metrics.MetricAdapter;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricProducer;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.resources.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * {@link MetricProducer} for OpenCensus metrics, which allows {@link MetricReader}s to read from
 * both OpenTelemetry and OpenCensus metrics.
 *
 * <p>To use, register with {@link SdkMeterProviderBuilder#registerMetricProducer(MetricProducer)}.
 */
public final class OpenCensusMetricProducer implements MetricProducer {
  private final MetricProducerManager openCensusMetricStorage;

  OpenCensusMetricProducer(MetricProducerManager openCensusMetricStorage) {
    this.openCensusMetricStorage = openCensusMetricStorage;
  }

  /**
   * Constructs a new {@link OpenCensusMetricProducer} that reports against the given {@link
   * Resource}.
   */
  public static MetricProducer create() {
    return new OpenCensusMetricProducer(Metrics.getExportComponent().getMetricProducerManager());
  }

  @Override
  public Collection<MetricData> produce(Resource resource) {
    List<MetricData> result = new ArrayList<>();
    openCensusMetricStorage
        .getAllMetricProducer()
        .forEach(
            producer ->
                producer
                    .getMetrics()
                    .forEach(metric -> result.add(MetricAdapter.convert(resource, metric))));
    return result;
  }
}
