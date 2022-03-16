/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opencensusshim.metrics;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.metrics.internal.export.AbstractMetricReader;
import io.opentelemetry.sdk.metrics.internal.export.MetricProducer;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Arrays;
import javax.annotation.Nullable;

/** {@link MetricReader} that appends OpenCensus metrics to anything read. */
final class OpenCensusAttachingMetricReader extends AbstractMetricReader {
  private final AbstractMetricReader adapted;

  OpenCensusAttachingMetricReader(MetricReader adapted) {
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

  @Nullable
  @Override
  public AggregationTemporality getPreferredTemporality() {
    return adapted.getPreferredTemporality();
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
