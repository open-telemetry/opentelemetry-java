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
      ActiveBatcher activeBatcher,
      @Nullable Callback<T> metricUpdater) {
    super(descriptor, meterProviderSharedState, meterSharedState, activeBatcher);
    this.metricUpdater = metricUpdater;
  }

  @Override
  List<MetricData> collectAll() {
    if (metricUpdater == null) {
      return Collections.emptyList();
    }
    collectLock.lock();
    try {
      final ActiveBatcher activeBatcher = getActiveBatcher();
      metricUpdater.update(newResult(activeBatcher));
      return activeBatcher.completeCollectionCycle();
    } finally {
      collectLock.unlock();
    }
  }

  abstract T newResult(ActiveBatcher activeBatcher);

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
        ActiveBatcher activeBatcher,
        @Nullable Callback<LongResult> metricUpdater) {
      super(descriptor, meterProviderSharedState, meterSharedState, activeBatcher, metricUpdater);
    }

    @Override
    LongResultSdk newResult(ActiveBatcher activeBatcher) {
      return new LongResultSdk(activeBatcher);
    }

    private static final class LongResultSdk implements LongResult {

      private final ActiveBatcher activeBatcher;

      private LongResultSdk(ActiveBatcher activeBatcher) {
        this.activeBatcher = activeBatcher;
      }

      @Override
      public void observe(long sum, Labels labels) {
        Aggregator aggregator = activeBatcher.getAggregator();
        aggregator.recordLong(sum);
        activeBatcher.batch(labels, aggregator, /* mappedAggregator= */ false);
      }
    }
  }

  static class AbstractDoubleAsynchronousInstrument
      extends AbstractAsynchronousInstrument<DoubleResult> {
    AbstractDoubleAsynchronousInstrument(
        InstrumentDescriptor descriptor,
        MeterProviderSharedState meterProviderSharedState,
        MeterSharedState meterSharedState,
        ActiveBatcher activeBatcher,
        @Nullable Callback<DoubleResult> metricUpdater) {
      super(descriptor, meterProviderSharedState, meterSharedState, activeBatcher, metricUpdater);
    }

    @Override
    DoubleResultSdk newResult(ActiveBatcher activeBatcher) {
      return new DoubleResultSdk(activeBatcher);
    }

    private static final class DoubleResultSdk implements DoubleResult {

      private final ActiveBatcher activeBatcher;

      private DoubleResultSdk(ActiveBatcher activeBatcher) {
        this.activeBatcher = activeBatcher;
      }

      @Override
      public void observe(double sum, Labels labels) {
        Aggregator aggregator = activeBatcher.getAggregator();
        aggregator.recordDouble(sum);
        activeBatcher.batch(labels, aggregator, /* mappedAggregator= */ false);
      }
    }
  }
}
