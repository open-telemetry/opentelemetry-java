/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import io.opentelemetry.sdk.metrics.aggregation.Accumulation;
import javax.annotation.Nullable;

public final class NoopAggregator implements Aggregator {
  private static final Aggregator NOOP_AGGREGATOR = new NoopAggregator();
  private static final AggregatorFactory AGGREGATOR_FACTORY = () -> NOOP_AGGREGATOR;

  public static AggregatorFactory getFactory() {
    return AGGREGATOR_FACTORY;
  }

  @Nullable
  @Override
  public Accumulation accumulateThenReset() {
    return null;
  }

  @Override
  public void recordLong(long value) {
    // Noop
  }

  @Override
  public void recordDouble(double value) {
    // Noop
  }

  private NoopAggregator() {}
}
