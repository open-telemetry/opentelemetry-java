/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.api.metrics.AsynchronousInstrument;
import io.opentelemetry.sdk.metrics.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.data.MetricData;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.Nullable;

abstract class AbstractAsynchronousInstrument<T extends AsynchronousInstrument.Result>
    extends AbstractInstrument implements AsynchronousInstrument<T> {
  @Nullable private final Callback<T> metricUpdater;
  private final ReentrantLock collectLock = new ReentrantLock();

  AbstractAsynchronousInstrument(
      InstrumentDescriptor descriptor,
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState meterSharedState,
      InstrumentAccumulator instrumentAccumulator,
      @Nullable Callback<T> metricUpdater) {
    super(descriptor, meterProviderSharedState, meterSharedState, instrumentAccumulator);
    this.metricUpdater = metricUpdater;
  }

  @Override
  List<MetricData> collectAll() {
    if (metricUpdater == null) {
      return Collections.emptyList();
    }
    collectLock.lock();
    try {
      final InstrumentAccumulator instrumentAccumulator = getInstrumentAccumulator();
      metricUpdater.update(newResult(instrumentAccumulator));
      return instrumentAccumulator.completeCollectionCycle();
    } finally {
      collectLock.unlock();
    }
  }

  abstract T newResult(InstrumentAccumulator instrumentAccumulator);

  abstract static class Builder<B extends AbstractInstrument.Builder<?>>
      extends AbstractInstrument.Builder<B> {
    Builder(
        String name,
        MeterProviderSharedState meterProviderSharedState,
        MeterSharedState meterSharedState,
        MeterSdk meterSdk) {
      super(name, meterProviderSharedState, meterSharedState, meterSdk);
    }
  }

  static class AbstractLongAsynchronousInstrument
      extends AbstractAsynchronousInstrument<LongResult> {
    AbstractLongAsynchronousInstrument(
        InstrumentDescriptor descriptor,
        MeterProviderSharedState meterProviderSharedState,
        MeterSharedState meterSharedState,
        InstrumentAccumulator instrumentAccumulator,
        @Nullable Callback<LongResult> metricUpdater) {
      super(
          descriptor,
          meterProviderSharedState,
          meterSharedState,
          instrumentAccumulator,
          metricUpdater);
    }

    @Override
    LongResultSdk newResult(InstrumentAccumulator instrumentAccumulator) {
      return new LongResultSdk(instrumentAccumulator);
    }

    private static final class LongResultSdk implements LongResult {

      private final InstrumentAccumulator instrumentAccumulator;

      private LongResultSdk(InstrumentAccumulator instrumentAccumulator) {
        this.instrumentAccumulator = instrumentAccumulator;
      }

      @Override
      public void observe(long sum, Labels labels) {
        Aggregator aggregator = instrumentAccumulator.getAggregator();
        aggregator.recordLong(sum);
        instrumentAccumulator.batch(labels, aggregator, /* mappedAggregator= */ false);
      }
    }
  }

  static class AbstractDoubleAsynchronousInstrument
      extends AbstractAsynchronousInstrument<DoubleResult> {
    AbstractDoubleAsynchronousInstrument(
        InstrumentDescriptor descriptor,
        MeterProviderSharedState meterProviderSharedState,
        MeterSharedState meterSharedState,
        InstrumentAccumulator instrumentAccumulator,
        @Nullable Callback<DoubleResult> metricUpdater) {
      super(
          descriptor,
          meterProviderSharedState,
          meterSharedState,
          instrumentAccumulator,
          metricUpdater);
    }

    @Override
    DoubleResultSdk newResult(InstrumentAccumulator instrumentAccumulator) {
      return new DoubleResultSdk(instrumentAccumulator);
    }

    private static final class DoubleResultSdk implements DoubleResult {

      private final InstrumentAccumulator instrumentAccumulator;

      private DoubleResultSdk(InstrumentAccumulator instrumentAccumulator) {
        this.instrumentAccumulator = instrumentAccumulator;
      }

      @Override
      public void observe(double sum, Labels labels) {
        Aggregator aggregator = instrumentAccumulator.getAggregator();
        aggregator.recordDouble(sum);
        instrumentAccumulator.batch(labels, aggregator, /* mappedAggregator= */ false);
      }
    }
  }
}
