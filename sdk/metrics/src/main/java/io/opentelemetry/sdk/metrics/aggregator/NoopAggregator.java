/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.metrics.data.MetricData.Point;
import javax.annotation.Nullable;

public final class NoopAggregator implements Aggregator {
  private static final Aggregator NOOP_AGGREGATOR = new NoopAggregator();
  private static final AggregatorFactory AGGREGATOR_FACTORY = () -> NOOP_AGGREGATOR;

  public static AggregatorFactory getFactory() {
    return AGGREGATOR_FACTORY;
  }

  @Override
  public void mergeToAndReset(Aggregator aggregator) {
    // Noop
  }

  @Nullable
  @Override
  public Point toPoint(long startEpochNanos, long epochNanos, Labels labels) {
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

  @Override
  public boolean hasRecordings() {
    return false;
  }

  private NoopAggregator() {}
}
