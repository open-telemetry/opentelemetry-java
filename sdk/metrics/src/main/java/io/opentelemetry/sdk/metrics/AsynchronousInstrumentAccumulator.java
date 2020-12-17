/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.metrics.AsynchronousInstrument;
import io.opentelemetry.sdk.metrics.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.data.MetricData;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import javax.annotation.Nullable;

final class AsynchronousInstrumentAccumulator {
  private final ReentrantLock collectLock = new ReentrantLock();
  private final InstrumentProcessor instrumentProcessor;
  private final Runnable metricUpdater;

  static AsynchronousInstrumentAccumulator doubleAsynchronousAccumulator(
      InstrumentProcessor instrumentProcessor,
      @Nullable Consumer<AsynchronousInstrument.DoubleResult> metricUpdater) {
    // TODO: Decide what to do with null updater.
    if (metricUpdater == null) {
      return new AsynchronousInstrumentAccumulator(instrumentProcessor, () -> {});
    }
    AsynchronousInstrument.DoubleResult result =
        (value, labels) -> {
          Aggregator aggregator = instrumentProcessor.getAggregator();
          aggregator.recordDouble(value);
          instrumentProcessor.batch(labels, aggregator, /* mappedAggregator= */ false);
        };

    return new AsynchronousInstrumentAccumulator(
        instrumentProcessor, () -> metricUpdater.accept(result));
  }

  static AsynchronousInstrumentAccumulator longAsynchronousAccumulator(
      InstrumentProcessor instrumentProcessor,
      @Nullable Consumer<AsynchronousInstrument.LongResult> metricUpdater) {
    // TODO: Decide what to do with null updater.
    if (metricUpdater == null) {
      return new AsynchronousInstrumentAccumulator(instrumentProcessor, () -> {});
    }

    AsynchronousInstrument.LongResult result =
        (value, labels) -> {
          Aggregator aggregator = instrumentProcessor.getAggregator();
          aggregator.recordLong(value);
          instrumentProcessor.batch(labels, aggregator, /* mappedAggregator= */ false);
        };

    return new AsynchronousInstrumentAccumulator(
        instrumentProcessor, () -> metricUpdater.accept(result));
  }

  private AsynchronousInstrumentAccumulator(
      InstrumentProcessor instrumentProcessor, Runnable metricUpdater) {
    this.metricUpdater = metricUpdater;
    this.instrumentProcessor = instrumentProcessor;
  }

  public List<MetricData> collectAll() {
    collectLock.lock();
    try {
      metricUpdater.run();
      return instrumentProcessor.completeCollectionCycle();
    } finally {
      collectLock.unlock();
    }
  }
}
