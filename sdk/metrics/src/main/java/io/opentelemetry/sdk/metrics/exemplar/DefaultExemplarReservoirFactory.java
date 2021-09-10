/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.exemplar;

import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.metrics.view.Aggregation;
import io.opentelemetry.sdk.metrics.view.ExplicitBucketHistogramAggregation;

class DefaultExemplarReservoirFactory implements ExemplarReservoirFactory {

  static final ExemplarReservoirFactory INSTANCE = new DefaultExemplarReservoirFactory();

  private DefaultExemplarReservoirFactory() {}

  @Override
  public ExemplarReservoir createReservoir(Aggregation aggregation) {
    Clock clock = Clock.getDefault();
    // For histograms, line up reservoirs with buckets.
    if (aggregation instanceof ExplicitBucketHistogramAggregation) {
      return HistogramBucketExemplarReservoir.create(
          clock, ((ExplicitBucketHistogramAggregation) aggregation).getBucketBoundaries());
    }
    // By default, reduce threading contention.
    return new FixedSizeExemplarReservoir(clock, Runtime.getRuntime().availableProcessors());
  }
}
