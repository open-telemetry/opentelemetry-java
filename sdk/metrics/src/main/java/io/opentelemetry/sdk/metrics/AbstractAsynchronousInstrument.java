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
import java.util.function.Consumer;
import javax.annotation.Nullable;

abstract class AbstractAsynchronousInstrument<T extends AsynchronousInstrument.Result>
    extends AbstractInstrument implements AsynchronousInstrument {
  @Nullable private final Consumer<T> metricUpdater;
  private final ReentrantLock collectLock = new ReentrantLock();
  private final InstrumentProcessor instrumentProcessor;

  AbstractAsynchronousInstrument(
      InstrumentDescriptor descriptor,
      InstrumentProcessor instrumentProcessor,
      @Nullable Consumer<T> metricUpdater) {
    super(descriptor);
    this.metricUpdater = metricUpdater;
    this.instrumentProcessor = instrumentProcessor;
  }

  @Override
  List<MetricData> collectAll() {
    if (metricUpdater == null) {
      return Collections.emptyList();
    }
    collectLock.lock();
    try {
      metricUpdater.accept(newResult(instrumentProcessor));
      return instrumentProcessor.completeCollectionCycle();
    } finally {
      collectLock.unlock();
    }
  }

  abstract T newResult(InstrumentProcessor instrumentProcessor);

  abstract static class Builder<B extends AbstractInstrument.Builder<?>>
      extends AbstractInstrument.Builder<B> {
    Builder(
        String name,
        MeterProviderSharedState meterProviderSharedState,
        MeterSharedState meterSharedState) {
      super(name, meterProviderSharedState, meterSharedState);
    }
  }

  static class AbstractLongAsynchronousInstrument
      extends AbstractAsynchronousInstrument<LongResult> {
    AbstractLongAsynchronousInstrument(
        InstrumentDescriptor descriptor,
        InstrumentProcessor instrumentProcessor,
        @Nullable Consumer<LongResult> metricUpdater) {
      super(descriptor, instrumentProcessor, metricUpdater);
    }

    @Override
    LongResultSdk newResult(InstrumentProcessor instrumentProcessor) {
      return new LongResultSdk(instrumentProcessor);
    }

    private static final class LongResultSdk implements LongResult {

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

  static class AbstractDoubleAsynchronousInstrument
      extends AbstractAsynchronousInstrument<DoubleResult> {
    AbstractDoubleAsynchronousInstrument(
        InstrumentDescriptor descriptor,
        InstrumentProcessor instrumentProcessor,
        @Nullable Consumer<DoubleResult> metricUpdater) {
      super(descriptor, instrumentProcessor, metricUpdater);
    }

    @Override
    DoubleResultSdk newResult(InstrumentProcessor instrumentProcessor) {
      return new DoubleResultSdk(instrumentProcessor);
    }

    private static final class DoubleResultSdk implements DoubleResult {

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
  }
}
