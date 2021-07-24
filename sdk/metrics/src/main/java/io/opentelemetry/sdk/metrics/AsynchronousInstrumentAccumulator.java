/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.ObservableDoubleMeasurement;
import io.opentelemetry.api.metrics.ObservableLongMeasurement;
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
      Consumer<ObservableDoubleMeasurement> metricUpdater) {
    Aggregator<T> aggregator =
        getAggregator(meterProviderSharedState, meterSharedState, descriptor);
    InstrumentProcessor<T> instrumentProcessor =
        new InstrumentProcessor<>(aggregator, meterProviderSharedState.getStartEpochNanos());

    LabelsProcessor labelsProcessor =
        getLabelsProcessor(meterProviderSharedState, meterSharedState, descriptor);
        // TODO: fix
    ObservableDoubleMeasurement result =
        (value, labels) ->
            instrumentProcessor.batch(
                labelsProcessor.onLabelsBound(Context.current(), labels),
                aggregator.accumulateDouble(value));

    return new AsynchronousInstrumentAccumulator(
        instrumentProcessor, () -> metricUpdater.accept(result));
  }

  static <T> AsynchronousInstrumentAccumulator longAsynchronousAccumulator(
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState meterSharedState,
      InstrumentDescriptor descriptor,
      Consumer<ObservableLongMeasurement> metricUpdater) {
    Aggregator<T> aggregator =
        getAggregator(meterProviderSharedState, meterSharedState, descriptor);
    final InstrumentProcessor<T> instrumentProcessor =
        new InstrumentProcessor<>(aggregator, meterProviderSharedState.getStartEpochNanos());

    final LabelsProcessor labelsProcessor =
        getLabelsProcessor(meterProviderSharedState, meterSharedState, descriptor);
    final ObservableLongMeasurement result = new ObservableLongMeasurement() {

      @Override
      public void observe(long value, Attributes attributes) {
            instrumentProcessor.batch(
                labelsProcessor.onLabelsBound(Context.current(), labels),
                aggregator.accumulateLong(value));
      }

      @Override
      public void observe(long value) {
        observe(value, Attributes.empty());        
      }

    };
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
