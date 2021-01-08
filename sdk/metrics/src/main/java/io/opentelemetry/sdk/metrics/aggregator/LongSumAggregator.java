/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.accumulation.LongAccumulation;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;

public final class LongSumAggregator implements Aggregator<LongAccumulation> {
  private static final LongSumAggregator INSTANCE = new LongSumAggregator();

  /**
   * Returns the instance of this {@link Aggregator}.
   *
   * @return the instance of this {@link Aggregator}.
   */
  public static Aggregator<LongAccumulation> getInstance() {
    return INSTANCE;
  }

  private LongSumAggregator() {}

  @Override
  public AggregatorHandle<LongAccumulation> createHandle() {
    return new Handle();
  }

  @Override
  public LongAccumulation accumulateLong(long value) {
    return LongAccumulation.create(value);
  }

  @Override
  public LongAccumulation merge(LongAccumulation a1, LongAccumulation a2) {
    return LongAccumulation.create(a1.getValue() + a2.getValue());
  }

  @Override
  public MetricData toMetricData(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      InstrumentDescriptor descriptor,
      Map<Labels, LongAccumulation> accumulationByLabels,
      long startEpochNanos,
      long epochNanos) {
    List<MetricData.LongPoint> points =
        MetricDataUtils.toLongPointList(accumulationByLabels, startEpochNanos, epochNanos);
    boolean isMonotonic =
        descriptor.getType() == InstrumentType.COUNTER
            || descriptor.getType() == InstrumentType.SUM_OBSERVER;
    return MetricDataUtils.toLongSumMetricData(
        resource, instrumentationLibraryInfo, descriptor, points, isMonotonic);
  }

  static final class Handle extends AggregatorHandle<LongAccumulation> {
    private final LongAdder current = new LongAdder();

    @Override
    protected LongAccumulation doAccumulateThenReset() {
      return LongAccumulation.create(this.current.sumThenReset());
    }

    @Override
    public void doRecordLong(long value) {
      current.add(value);
    }
  }
}
