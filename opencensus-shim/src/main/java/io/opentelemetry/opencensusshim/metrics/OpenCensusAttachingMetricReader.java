/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opencensusshim.metrics;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.export.CollectionRegistration;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.metrics.internal.export.MetricProducer;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Arrays;

/** {@link MetricReader} that appends OpenCensus metrics to anything read. */
final class OpenCensusAttachingMetricReader implements MetricReader {
  private final MetricReader adapted;

  OpenCensusAttachingMetricReader(MetricReader adapted) {
    this.adapted = adapted;
  }

  @Override
  public void register(CollectionRegistration registration) {
    // TODO: Find a way to pull the resource off of the SDK.
    adapted.register(
        new MultiMetricProducer(
            Arrays.asList(
                MetricProducer.asMetricProducer(registration),
                OpenCensusMetricProducer.create(Resource.getDefault()))));
  }

  @Override
  public AggregationTemporality getAggregationTemporality(InstrumentType instrumentType) {
    return adapted.getAggregationTemporality(instrumentType);
  }

  @Override
  public CompletableResultCode forceFlush() {
    return adapted.forceFlush();
  }

  @Override
  public CompletableResultCode shutdown() {
    return adapted.shutdown();
  }
}
