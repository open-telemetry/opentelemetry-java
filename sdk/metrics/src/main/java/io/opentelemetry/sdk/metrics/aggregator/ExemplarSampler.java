/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.metrics.state.ExemplarReservoir;
import io.opentelemetry.sdk.metrics.state.SingleExemplarReservoir;

/**
 * An interface that provides an ExemplarResorvoir implementation for collecting exemplars of a
 * given metric point.
 */
@FunctionalInterface
public interface ExemplarSampler {

  ExemplarReservoir createReservoir(Aggregator<?> aggregator);

  /** Never samples exemplars. */
  public static ExemplarSampler NEVER = (agg) -> ExemplarReservoir.EMPTY;

  /** Sample measurements that were recorded during a sampled trace. */
  public static ExemplarSampler WITH_SAMPLED_TRACES =
      (agg) -> {
        // TODO - for histograms, make bigger resorvoirs.
        // TODO - pull clock from meterprovider shared state.
        return new SingleExemplarReservoir(Clock.getDefault());
      };
}
