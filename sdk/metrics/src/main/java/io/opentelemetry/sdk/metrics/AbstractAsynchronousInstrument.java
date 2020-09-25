/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.common.Labels;
import io.opentelemetry.metrics.AsynchronousInstrument;
import io.opentelemetry.sdk.metrics.BatchObserverSdk.DoubleObservation;
import io.opentelemetry.sdk.metrics.BatchObserverSdk.LongObservation;
import io.opentelemetry.sdk.metrics.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricData.Descriptor;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.Nullable;

abstract class AbstractAsynchronousInstrument<
        T extends AsynchronousInstrument.Result, U extends Number>
    extends AbstractInstrument implements AsynchronousInstrument<T, U> {
  @Nullable private volatile Callback<T> metricUpdater = null;
  private final ReentrantLock collectLock = new ReentrantLock();

  AbstractAsynchronousInstrument(
      InstrumentDescriptor descriptor,
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState meterSharedState,
      ActiveBatcher activeBatcher) {
    super(descriptor, meterProviderSharedState, meterSharedState, activeBatcher);
  }

  @Override
  List<MetricData> collectAll() {
    Callback<T> currentMetricUpdater = metricUpdater;
    if (currentMetricUpdater == null) {
      return Collections.emptyList();
    }
    collectLock.lock();
    try {
      final ActiveBatcher activeBatcher = getActiveBatcher();
      currentMetricUpdater.update(newResult(activeBatcher));
      return activeBatcher.completeCollectionCycle();
    } finally {
      collectLock.unlock();
    }
  }

  @Override
  public void setCallback(Callback<T> callback) {
    this.metricUpdater = Objects.requireNonNull(callback, "callback");
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
      extends AbstractAsynchronousInstrument<LongResult, Long> {
    AbstractLongAsynchronousInstrument(
        InstrumentDescriptor descriptor,
        MeterProviderSharedState meterProviderSharedState,
        MeterSharedState meterSharedState,
        ActiveBatcher activeBatcher) {
      super(descriptor, meterProviderSharedState, meterSharedState, activeBatcher);
    }

    @Override
    LongResultSdk newResult(ActiveBatcher activeBatcher) {
      return new LongResultSdk(activeBatcher);
    }

    @Override
    public Observation observation(Long observation) {
      return new LongObservationSdk(this.getActiveBatcher(), observation);
    }

    private static final class LongObservationSdk implements LongObservation {
      private final ActiveBatcher activeBatcher;
      private final long value;

      private LongObservationSdk(ActiveBatcher activeBatcher, long value) {
        this.activeBatcher = activeBatcher;
        this.value = value;
      }

      @Override
      public ObservationType getType() {
        return ObservationType.LONG_OBSERVATION;
      }

      @Override
      public Aggregator getAggregator() {
        return this.activeBatcher.getAggregator();
      }

      @Override
      public Descriptor getDescriptor() {
        return this.activeBatcher.getDescriptor();
      }

      @Override
      public long getValue() {
        return this.value;
      }
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
      extends AbstractAsynchronousInstrument<DoubleResult, Double> {
    AbstractDoubleAsynchronousInstrument(
        InstrumentDescriptor descriptor,
        MeterProviderSharedState meterProviderSharedState,
        MeterSharedState meterSharedState,
        ActiveBatcher activeBatcher) {
      super(descriptor, meterProviderSharedState, meterSharedState, activeBatcher);
    }

    @Override
    DoubleResultSdk newResult(ActiveBatcher activeBatcher) {
      return new DoubleResultSdk(activeBatcher);
    }

    @Override
    public Observation observation(Double observation) {
      return new DoubleObservationSdk(this.getActiveBatcher(), observation);
    }

    private static final class DoubleObservationSdk implements DoubleObservation {

      private final ActiveBatcher activeBatcher;
      private final double value;

      private DoubleObservationSdk(ActiveBatcher activeBatcher, double value) {
        this.activeBatcher = activeBatcher;
        this.value = value;
      }

      @Override
      public ObservationType getType() {
        return ObservationType.DOUBLE_OBSERVATION;
      }

      @Override
      public Aggregator getAggregator() {
        return this.activeBatcher.getAggregator();
      }

      @Override
      public Descriptor getDescription() {
        return this.activeBatcher.getDescriptor();
      }

      @Override
      public double getValue() {
        return this.value;
      }
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
