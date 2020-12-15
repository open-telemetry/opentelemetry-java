/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.Labels;
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
  private final Consumer<InstrumentProcessor> metricUpdater;

  private AsynchronousInstrumentAccumulator(
      InstrumentProcessor instrumentProcessor, Consumer<InstrumentProcessor> metricUpdater) {
    this.metricUpdater = metricUpdater;
    this.instrumentProcessor = instrumentProcessor;
  }

  public List<MetricData> collectAll() {
    collectLock.lock();
    try {
      metricUpdater.accept(instrumentProcessor);
      return instrumentProcessor.completeCollectionCycle();
    } finally {
      collectLock.unlock();
    }
  }

  static AsynchronousInstrumentAccumulator doubleAsynchronousAccumulator(
      InstrumentProcessor instrumentProcessor,
      @Nullable Consumer<AsynchronousInstrument.DoubleResult> metricUpdater) {
    // TODO: Decide what to do with null updater.
    if (metricUpdater == null) {
      return new AsynchronousInstrumentAccumulator(instrumentProcessor, instrumentProcessor1 -> {});
    }
    DoubleResultSdk result = new DoubleResultSdk(instrumentProcessor);
    return new AsynchronousInstrumentAccumulator(
        instrumentProcessor, instrumentProcessor1 -> metricUpdater.accept(result));
  }

  static AsynchronousInstrumentAccumulator longAsynchronousAccumulator(
      InstrumentProcessor instrumentProcessor,
      @Nullable Consumer<AsynchronousInstrument.LongResult> metricUpdater) {
    // TODO: Decide what to do with null updater.
    if (metricUpdater == null) {
      return new AsynchronousInstrumentAccumulator(instrumentProcessor, instrumentProcessor1 -> {});
    }
    LongResultSdk result = new LongResultSdk(instrumentProcessor);
    return new AsynchronousInstrumentAccumulator(
        instrumentProcessor, instrumentProcessor1 -> metricUpdater.accept(result));
  }

  private static final class DoubleResultSdk implements AsynchronousInstrument.DoubleResult {
    private final InstrumentProcessor instrumentProcessor;

    private DoubleResultSdk(InstrumentProcessor instrumentProcessor) {
      this.instrumentProcessor = instrumentProcessor;
    }

    @Override
    public void observe(double sum, Labels labels) {
      Aggregator aggregator = instrumentProcessor.getAggregator();
      aggregator.recordDouble(sum);
      instrumentProcessor.batch(labels, aggregator, /* mappedAggregator= */ false);
    }
  }

  private static final class LongResultSdk implements AsynchronousInstrument.LongResult {
    private final InstrumentProcessor instrumentProcessor;

    private LongResultSdk(InstrumentProcessor instrumentProcessor) {
      this.instrumentProcessor = instrumentProcessor;
    }

    @Override
    public void observe(long sum, Labels labels) {
      Aggregator aggregator = instrumentProcessor.getAggregator();
      aggregator.recordLong(sum);
      instrumentProcessor.batch(labels, aggregator, /* mappedAggregator= */ false);
    }
  }
}
