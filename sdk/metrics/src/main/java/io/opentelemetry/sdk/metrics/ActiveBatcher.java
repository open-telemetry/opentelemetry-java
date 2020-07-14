/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.metrics.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.data.MetricData;
import java.util.List;

/**
 * Tracks a list of active Batchers used to aggregate measurements recorded by one {@code
 * Instrument}.
 *
 * <p>TODO: Add support for multiple "Batchers" in the same time.
 *
 * <p>TODO: Consider if support for changing batchers at runtime is needed.
 */
final class ActiveBatcher implements Batcher {
  private final Batcher batcher;

  ActiveBatcher(Batcher batcher) {
    this.batcher = batcher;
  }

  @Override
  public Aggregator getAggregator() {
    return batcher.getAggregator();
  }

  @Override
  public void batch(Labels labelSet, Aggregator aggregator, boolean mappedAggregator) {
    if (aggregator.hasRecordings()) {
      batcher.batch(labelSet, aggregator, mappedAggregator);
    }
  }

  @Override
  public List<MetricData> completeCollectionCycle() {
    return batcher.completeCollectionCycle();
  }

  @Override
  public boolean generatesDeltas() {
    return batcher.generatesDeltas();
  }
}
