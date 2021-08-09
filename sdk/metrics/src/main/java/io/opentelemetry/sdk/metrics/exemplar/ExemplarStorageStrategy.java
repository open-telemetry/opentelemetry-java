/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.exemplar;

import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.metrics.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.aggregator.DoubleHistogramAggregator;

/** A strategy for storing exemplars. */
@FunctionalInterface
public interface ExemplarStorageStrategy {
  /** Constructs a new sampling resorvoir for a given aggregator. */
  ExemplarReservoir createReservoir(Aggregator<?> aggregator);

  /** A sampler which will never store Exemplars. */
  static final ExemplarStorageStrategy ALWAYS_OFF = (agg) -> ExemplarReservoir.EMPTY;
  /**
   * Default exemplar storage configuraiton. Optimised for reduced threading contention and
   * accuracy.
   */
  static final ExemplarStorageStrategy DEFAULT =
      (agg) -> {
        // For histograms, use bucket strategy
        if (agg instanceof DoubleHistogramAggregator) {
          DoubleHistogramAggregator dag = ((DoubleHistogramAggregator) agg);
          return new HistogramBucketExemplarReservoir(
              Clock.getDefault(), dag.getBucketCount(), dag::findBucketIndex);
        }
        // By default, attempt to reduce threading contention.
        return new FixedSizeExemplarReservoir(
            Clock.getDefault(), Runtime.getRuntime().availableProcessors());
      };
}
