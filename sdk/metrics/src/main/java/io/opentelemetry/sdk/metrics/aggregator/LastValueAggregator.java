/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.DoubleGaugeData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.instrument.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.instrument.Measurement;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Aggregator that aggregates recorded values by storing the last recorded value.
 *
 * <p>Limitation: The current implementation does not store a time when the value was recorded, so
 * merging multiple LastValueAggregators will not preserve the ordering of records. This is not a
 * problem because LastValueAggregator is currently only available for Observers which record all
 * values once.
 */
public class LastValueAggregator extends AbstractAggregator<DoubleAccumulation> {
  private final InstrumentDescriptor instrument;
  private final Resource resource;
  private final InstrumentationLibraryInfo instrumentationLibrary;
  private final AggregationTemporality temporality;
  private final ExemplarSampler sampler;

  public LastValueAggregator(
      InstrumentDescriptor instrument,
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibrary,
      long startEpochNanos,
      AggregationTemporality temporality,
      ExemplarSampler sampler) {
    super(startEpochNanos);
    this.instrument = instrument;
    this.resource = resource;
    this.instrumentationLibrary = instrumentationLibrary;
    this.temporality = temporality;
    this.sampler = sampler;
  }

  @Override
  public SynchronousHandle<DoubleAccumulation> createStreamStorage() {
    return new MyHandle(sampler);
  }

  // Note:  Storage handle has high contention and need atomic increments.
  static class MyHandle extends SynchronousHandle<DoubleAccumulation> {
    private final AtomicReference<Double> latest = new AtomicReference<>(null);

    MyHandle(ExemplarSampler sampler) {
      super(sampler);
    }

    @Override
    protected void doRecord(Measurement value) {
      latest.lazySet(value.asDouble().getValue());
    }

    @Override
    protected DoubleAccumulation doAccumulateThenReset(Iterable<Measurement> exemplars) {
      Double result = latest.getAndSet(null);
      if (result == null) {
        return null;
      }
      return DoubleAccumulation.create(result, exemplars);
    }
  }

  @Override
  protected boolean isStatefulCollector() {
    return (temporality == AggregationTemporality.CUMULATIVE)
        && instrument.getType().isSynchronous();
  }

  @Override
  DoubleAccumulation asyncAccumulation(Measurement measurement) {
    return DoubleAccumulation.create(measurement.asDouble().getValue());
  }

  @Override
  protected DoubleAccumulation merge(DoubleAccumulation current, DoubleAccumulation accumulated) {
    return current;
  }

  @Override
  protected MetricData buildMetric(
      Map<Attributes, DoubleAccumulation> accumulated,
      long startEpochNanos,
      long lastEpochNanos,
      long epochNanos) {
    return MetricData.createDoubleGauge(
        resource,
        instrumentationLibrary,
        instrument.getName(),
        instrument.getDescription(),
        instrument.getUnit(),
        DoubleGaugeData.create(
            MetricDataUtils.toDoublePointList(accumulated, startEpochNanos, epochNanos)));
  }
}
