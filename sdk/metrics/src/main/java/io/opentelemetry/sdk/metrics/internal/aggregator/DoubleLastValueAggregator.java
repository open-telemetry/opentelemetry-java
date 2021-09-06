/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.data.DoubleGaugeData;
import io.opentelemetry.sdk.metrics.data.Exemplar;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.exemplar.ExemplarReservoir;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.resources.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Aggregator that aggregates recorded values by storing the last recorded value.
 *
 * <p>Limitation: The current implementation does not store a time when the value was recorded, so
 * merging multiple LastValueAggregators will not preserve the ordering of records. This is not a
 * problem because LastValueAggregator is currently only available for Observers which record all
 * values once.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@ThreadSafe
final class DoubleLastValueAggregator extends AbstractAggregator<Double> {
  private final Supplier<ExemplarReservoir> reservoirBuilder;

  DoubleLastValueAggregator(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      MetricDescriptor descriptor,
      Supplier<ExemplarReservoir> reservoirBuilder) {
    super(resource, instrumentationLibraryInfo, descriptor, /* stateful= */ true);
    this.reservoirBuilder = reservoirBuilder;
  }

  @Override
  public AggregatorHandle<Double> createHandle() {
    return new Handle(reservoirBuilder.get());
  }

  @Override
  public Double accumulateDouble(double value) {
    return value;
  }

  @Override
  public Double merge(Double a1, Double a2) {
    // TODO: Define the order between accumulation.
    return a2;
  }

  @Override
  public MetricData toMetricData(
      Map<Attributes, Double> accumulationByLabels,
      long startEpochNanos,
      long lastCollectionEpoch,
      long epochNanos) {
    return MetricData.createDoubleGauge(
        getResource(),
        getInstrumentationLibraryInfo(),
        getMetricDescriptor().getName(),
        getMetricDescriptor().getDescription(),
        getMetricDescriptor().getUnit(),
        DoubleGaugeData.create(
            MetricDataUtils.toDoublePointList(accumulationByLabels, 0, epochNanos)));
  }

  static final class Handle extends AggregatorHandle<Double> {
    @Nullable private static final Double DEFAULT_VALUE = null;
    private final AtomicReference<Double> current = new AtomicReference<>(DEFAULT_VALUE);

    private Handle(ExemplarReservoir reservoir) {
      super(reservoir);
    }

    @Override
    protected Double doAccumulateThenReset(List<Exemplar> exemplars) {
      return this.current.getAndSet(DEFAULT_VALUE);
    }

    @Override
    protected void doRecordDouble(double value) {
      current.set(value);
    }
  }
}
