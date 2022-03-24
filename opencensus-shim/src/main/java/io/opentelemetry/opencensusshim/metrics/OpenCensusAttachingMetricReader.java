/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opencensusshim.metrics;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.metrics.internal.export.AbstractMetricReader;
import io.opentelemetry.sdk.metrics.internal.export.MetricProducer;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Arrays;

/** {@link MetricReader} that appends OpenCensus metrics to anything read. */
final class OpenCensusAttachingMetricReader extends AbstractMetricReader {
  private final AbstractMetricReader adapted;

  OpenCensusAttachingMetricReader(MetricReader adapted) {
    super(adapted::getAggregationTemporality);
    this.adapted = AbstractMetricReader.asAbstractMetricReader(adapted);
  }

  @Override
  protected void registerMetricProducer(MetricProducer metricProducer) {
    // TODO: Find a way to pull the resource off of the SDK.
    AbstractMetricReader.registerMetricProducer(
        new MultiMetricProducer(
            Arrays.asList(metricProducer, OpenCensusMetricProducer.create(Resource.getDefault()))),
        adapted);
  }

  @Override
  public CompletableResultCode flush() {
    return adapted.flush();
  }

  @Override
  public CompletableResultCode shutdown() {
    return adapted.shutdown();
  }
}
