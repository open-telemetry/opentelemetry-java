/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.LongGaugeData;
import io.opentelemetry.sdk.metrics.data.LongSumData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nullable;

/**
 * Aggregator that aggregates recorded values by storing the last recorded value.
 *
 * <p>Limitation: The current implementation does not store a time when the value was recorded, so
 * merging multiple LastValueAggregators will not preserve the ordering of records. This is not a
 * problem because LastValueAggregator is currently only available for Observers which record all
 * values once.
 */
final class LongLastValueAggregator extends AbstractAggregator<Long> {
  LongLastValueAggregator(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      InstrumentDescriptor descriptor) {
    super(resource, instrumentationLibraryInfo, descriptor);
  }

  @Override
  public AggregatorHandle<Long> createHandle() {
    return new Handle();
  }

  @Override
  public Long accumulateLong(long value) {
    return value;
  }

  @Override
  public Long merge(Long a1, Long a2) {
    // TODO: Define the order between accumulation.
    return a2;
  }

  @Override
  public MetricData toMetricData(
      Map<Labels, Long> accumulationByLabels, long startEpochNanos, long epochNanos) {
    switch (getInstrumentDescriptor().getType()) {
      case SUM_OBSERVER:
        return MetricData.createLongSum(
            getResource(),
            getInstrumentationLibraryInfo(),
            getInstrumentDescriptor().getName(),
            getInstrumentDescriptor().getDescription(),
            getInstrumentDescriptor().getUnit(),
            LongSumData.create(
                /* isMonotonic= */ true,
                AggregationTemporality.CUMULATIVE,
                MetricDataUtils.toLongPointList(
                    accumulationByLabels, startEpochNanos, epochNanos)));
      case UP_DOWN_SUM_OBSERVER:
        return MetricData.createLongSum(
            getResource(),
            getInstrumentationLibraryInfo(),
            getInstrumentDescriptor().getName(),
            getInstrumentDescriptor().getDescription(),
            getInstrumentDescriptor().getUnit(),
            LongSumData.create(
                /* isMonotonic= */ false,
                AggregationTemporality.CUMULATIVE,
                MetricDataUtils.toLongPointList(
                    accumulationByLabels, startEpochNanos, epochNanos)));
      case VALUE_OBSERVER:
        return MetricData.createLongGauge(
            getResource(),
            getInstrumentationLibraryInfo(),
            getInstrumentDescriptor().getName(),
            getInstrumentDescriptor().getDescription(),
            getInstrumentDescriptor().getUnit(),
            LongGaugeData.create(
                MetricDataUtils.toLongPointList(
                    accumulationByLabels, startEpochNanos, epochNanos)));
      case COUNTER:
      case UP_DOWN_COUNTER:
      case VALUE_RECORDER:
    }
    return null;
  }

  static final class Handle extends AggregatorHandle<Long> {
    @Nullable private static final Long DEFAULT_VALUE = null;
    private final AtomicReference<Long> current = new AtomicReference<>(DEFAULT_VALUE);

    @Override
    protected Long doAccumulateThenReset() {
      return this.current.getAndSet(DEFAULT_VALUE);
    }

    @Override
    protected void doRecordLong(long value) {
      current.set(value);
    }
  }
}
