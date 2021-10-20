/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.data.ExemplarData;
import io.opentelemetry.sdk.metrics.data.LongGaugeData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricDataBuilder;
import io.opentelemetry.sdk.metrics.exemplar.ExemplarReservoir;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.resources.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import javax.annotation.Nullable;

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
final class LongLastValueAggregator extends AbstractAggregator<LongAccumulation> {
  private final Supplier<ExemplarReservoir> reservoirSupplier;

  LongLastValueAggregator(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      MetricDescriptor descriptor,
      Supplier<ExemplarReservoir> reservoirSupplier) {
    super(resource, instrumentationLibraryInfo, descriptor, /* stateful= */ false);
    this.reservoirSupplier = reservoirSupplier;
  }

  @Override
  public AggregatorHandle<LongAccumulation> createHandle() {
    return new Handle(reservoirSupplier.get());
  }

  @Override
  public LongAccumulation accumulateLong(long value) {
    return LongAccumulation.create(value);
  }

  @Override
  public LongAccumulation merge(LongAccumulation previous, LongAccumulation current) {
    return current;
  }

  @Override
  public MetricData toMetricData(
      Map<Attributes, LongAccumulation> accumulationByLabels,
      long startEpochNanos,
      long lastCollectionEpoch,
      long epochNanos) {
    return MetricDataBuilder.createLongGauge(
        getResource(),
        getInstrumentationLibraryInfo(),
        getMetricDescriptor().getName(),
        getMetricDescriptor().getDescription(),
        getMetricDescriptor().getUnit(),
        LongGaugeData.create(MetricDataUtils.toLongPointList(accumulationByLabels, 0, epochNanos)));
  }

  static final class Handle extends AggregatorHandle<LongAccumulation> {
    @Nullable private static final Long DEFAULT_VALUE = null;
    private final AtomicReference<Long> current = new AtomicReference<>(DEFAULT_VALUE);

    Handle(ExemplarReservoir exemplarReservoir) {
      super(exemplarReservoir);
    }

    @Override
    protected LongAccumulation doAccumulateThenReset(List<ExemplarData> exemplars) {
      return LongAccumulation.create(this.current.getAndSet(DEFAULT_VALUE), exemplars);
    }

    @Override
    protected void doRecordLong(long value) {
      current.set(value);
    }
  }
}
