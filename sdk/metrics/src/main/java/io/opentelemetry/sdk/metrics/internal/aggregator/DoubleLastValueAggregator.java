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
final class DoubleLastValueAggregator extends AbstractAggregator<DoubleAccumulation> {
  private final Supplier<ExemplarReservoir> reservoirSupplier;

  DoubleLastValueAggregator(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      MetricDescriptor descriptor,
      Supplier<ExemplarReservoir> reservoirSupplier) {
    super(resource, instrumentationLibraryInfo, descriptor, /* stateful= */ true);
    this.reservoirSupplier = reservoirSupplier;
  }

  @Override
  public AggregatorHandle<DoubleAccumulation> createHandle() {
    return new Handle(reservoirSupplier.get());
  }

  @Override
  public DoubleAccumulation accumulateDouble(double value) {
    return DoubleAccumulation.create(value);
  }

  @Override
  public DoubleAccumulation merge(DoubleAccumulation previous, DoubleAccumulation current) {
    return current;
  }

  @Override
  public MetricData toMetricData(
      Map<Attributes, DoubleAccumulation> accumulationByLabels,
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

  static final class Handle extends AggregatorHandle<DoubleAccumulation> {
    @Nullable private static final Double DEFAULT_VALUE = null;
    private final AtomicReference<Double> current = new AtomicReference<>(DEFAULT_VALUE);

    private Handle(ExemplarReservoir reservoir) {
      super(reservoir);
    }

    @Override
    protected DoubleAccumulation doAccumulateThenReset(List<Exemplar> exemplars) {
      return DoubleAccumulation.create(this.current.getAndSet(DEFAULT_VALUE), exemplars);
    }

    @Override
    protected void doRecordDouble(double value) {
      current.set(value);
    }
  }
}
