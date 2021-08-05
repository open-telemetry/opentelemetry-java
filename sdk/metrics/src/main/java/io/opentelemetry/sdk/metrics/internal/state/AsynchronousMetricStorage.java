/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.ObservableDoubleMeasurement;
import io.opentelemetry.api.metrics.ObservableLongMeasurement;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.metrics.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.metrics.processor.LabelsProcessor;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public final class AsynchronousMetricStorage implements MetricStorage {
  private final MetricDescriptor metricDescriptor;
  private final ReentrantLock collectLock = new ReentrantLock();
  private final InstrumentProcessor<?> instrumentProcessor;
  private final Runnable metricUpdater;

  public static <T> AsynchronousMetricStorage doubleAsynchronousAccumulator(
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState meterSharedState,
      InstrumentDescriptor descriptor,
      Consumer<ObservableDoubleMeasurement> metricUpdater) {
    Aggregator<T> aggregator = meterProviderSharedState.getAggregator(meterSharedState, descriptor);
    final InstrumentProcessor<T> instrumentProcessor =
        new InstrumentProcessor<>(aggregator, meterProviderSharedState.getStartEpochNanos());

    final LabelsProcessor labelsProcessor =
        meterProviderSharedState.getLabelsProcessor(meterSharedState, descriptor);

    final ObservableDoubleMeasurement result =
        new ObservableDoubleMeasurement() {
          @Override
          public void observe(double value, Attributes attributes) {
            instrumentProcessor.batch(
                labelsProcessor.onLabelsBound(Context.current(), attributes),
                aggregator.accumulateDouble(value));
          }

          @Override
          public void observe(double value) {
            observe(value, Attributes.empty());
          }
        };

    return new AsynchronousMetricStorage(
        // TODO: View can change metric name/description.  Update this when wired in.
        MetricDescriptor.create(
            descriptor.getName(), descriptor.getDescription(), descriptor.getUnit()),
        instrumentProcessor,
        () -> metricUpdater.accept(result));
  }

  public static <T> AsynchronousMetricStorage longAsynchronousAccumulator(
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState meterSharedState,
      InstrumentDescriptor descriptor,
      Consumer<ObservableLongMeasurement> metricUpdater) {
    Aggregator<T> aggregator = meterProviderSharedState.getAggregator(meterSharedState, descriptor);
    final InstrumentProcessor<T> instrumentProcessor =
        new InstrumentProcessor<>(aggregator, meterProviderSharedState.getStartEpochNanos());

    final LabelsProcessor labelsProcessor =
        meterProviderSharedState.getLabelsProcessor(meterSharedState, descriptor);
    final ObservableLongMeasurement result =
        new ObservableLongMeasurement() {

          @Override
          public void observe(long value, Attributes attributes) {
            instrumentProcessor.batch(
                labelsProcessor.onLabelsBound(Context.current(), attributes),
                aggregator.accumulateLong(value));
          }

          @Override
          public void observe(long value) {
            observe(value, Attributes.empty());
          }
        };
    return new AsynchronousMetricStorage(
        // TODO: View can change metric name/description.  Update this when wired in.
        MetricDescriptor.create(
            descriptor.getName(), descriptor.getDescription(), descriptor.getUnit()),
        instrumentProcessor,
        () -> metricUpdater.accept(result));
  }

  private AsynchronousMetricStorage(
      MetricDescriptor metricDescriptor,
      InstrumentProcessor<?> instrumentProcessor,
      Runnable metricUpdater) {
    this.metricDescriptor = metricDescriptor;
    this.instrumentProcessor = instrumentProcessor;
    this.metricUpdater = metricUpdater;
  }

  @Override
  public MetricData collectAndReset(long startEpochNanos, long epochNanos) {
    collectLock.lock();
    try {
      metricUpdater.run();
      return instrumentProcessor.completeCollectionCycle(epochNanos);
    } finally {
      collectLock.unlock();
    }
  }

  @Override
  public MetricDescriptor getMetricDescriptor() {
    return metricDescriptor;
  }
}
