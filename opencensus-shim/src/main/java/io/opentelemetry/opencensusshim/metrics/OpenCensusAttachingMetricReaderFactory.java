/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opencensusshim.metrics;

import io.opentelemetry.sdk.metrics.export.MetricProducer;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.metrics.export.MetricReaderFactory;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Arrays;

/** MetricReaderFactory that appends OpenCensus metrics to anything read. */
final class OpenCensusAttachingMetricReaderFactory implements MetricReaderFactory {
  private final MetricReaderFactory adapted;

  OpenCensusAttachingMetricReaderFactory(MetricReaderFactory adapted) {
    this.adapted = adapted;
  }

  @Override
  public MetricReader apply(MetricProducer producer) {
    // TODO: Find a way to pull the resource off of the SDK.
    return adapted.apply(
        new MultiMetricProducer(
            Arrays.asList(producer, OpenCensusMetricProducer.create(Resource.getDefault()))));
  }
}
