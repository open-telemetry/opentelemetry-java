/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.exemplar;

import io.opentelemetry.sdk.metrics.view.Aggregation;

/** A strategy for storing exemplars. */
@FunctionalInterface
public interface ExemplarStorageStrategy {
  /** Constructs a new sampling reservoir for a given aggregator. */
  ExemplarReservoir createReservoir(Aggregation aggregator);

  /** A sampler which will never store Exemplars. */
  static final ExemplarStorageStrategy ALWAYS_OFF = (agg) -> ExemplarReservoir.noSamples();
  /** Default exemplar storage configuration. */
  static final ExemplarStorageStrategy DEFAULT =
      (agg) -> {
        // TODO: implement
        return ExemplarReservoir.noSamples();
      };
}
