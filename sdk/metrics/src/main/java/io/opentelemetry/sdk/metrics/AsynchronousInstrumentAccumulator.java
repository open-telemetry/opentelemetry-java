/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.metrics.AsynchronousInstrument;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.metrics.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.processor.LabelsProcessor;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import javax.annotation.Nullable;

final class AsynchronousInstrumentAccumulator extends AbstractAccumulator {
  private final ReentrantLock collectLock = new ReentrantLock();
  private final InstrumentProcessor<?> instrumentProcessor;
  private final Runnable metricUpdater;

  static <T> AsynchronousInstrumentAccumulator doubleAsynchronousAccumulator(
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState meterSharedState,
      InstrumentDescriptor descriptor,
      @Nullable Consumer<AsynchronousInstrument.DoubleResult> metricUpdater) {
    Aggregator<T> aggregator =
        getAggregator(meterProviderSharedState, meterSharedState, descriptor);
    InstrumentProcessor<T> instrumentProcessor =
        new InstrumentProcessor<>(aggregator, meterProviderSharedState.getStartEpochNanos());
    // TODO: Decide what to do with null updater.
    if (metricUpdater == null) {
      return new AsynchronousInstrumentAccumulator(instrumentProcessor, () -> {});
    }

    LabelsProcessor labelsProcessor =
        getLabelsProcessor(meterProviderSharedState, meterSharedState, descriptor);
    System.out.println("double label processor " + labelsProcessor);
    Context ctx = Context.current();
    AsynchronousInstrument.DoubleResult result =
        (value, labels) ->
            instrumentProcessor.batch(
                labelsProcessor.onLabelsBound(ctx, labels), aggregator.accumulateDouble(value));

    return new AsynchronousInstrumentAccumulator(
        instrumentProcessor, () -> metricUpdater.accept(result));
  }

  static <T> AsynchronousInstrumentAccumulator longAsynchronousAccumulator(
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState meterSharedState,
      InstrumentDescriptor descriptor,
      @Nullable Consumer<AsynchronousInstrument.LongResult> metricUpdater) {
    Aggregator<T> aggregator =
        getAggregator(meterProviderSharedState, meterSharedState, descriptor);
    InstrumentProcessor<T> instrumentProcessor =
        new InstrumentProcessor<>(aggregator, meterProviderSharedState.getStartEpochNanos());
    // TODO: Decide what to do with null updater.
    if (metricUpdater == null) {
      return new AsynchronousInstrumentAccumulator(instrumentProcessor, () -> {});
    }

    LabelsProcessor labelsProcessor =
        getLabelsProcessor(meterProviderSharedState, meterSharedState, descriptor);
    System.out.println("long label processor " + labelsProcessor);
    Context ctx = Context.current();
    AsynchronousInstrument.LongResult result =
        (value, labels) ->
            instrumentProcessor.batch(
                labelsProcessor.onLabelsBound(ctx, labels), aggregator.accumulateLong(value));

    return new AsynchronousInstrumentAccumulator(
        instrumentProcessor, () -> metricUpdater.accept(result));
  }

  private AsynchronousInstrumentAccumulator(
      InstrumentProcessor<?> instrumentProcessor, Runnable metricUpdater) {
    this.instrumentProcessor = instrumentProcessor;
    this.metricUpdater = metricUpdater;
  }

  @Override
  List<MetricData> collectAll(long epochNanos) {
    collectLock.lock();
    try {
      metricUpdater.run();
      return instrumentProcessor.completeCollectionCycle(epochNanos);
    } finally {
      collectLock.unlock();
    }
  }
}
