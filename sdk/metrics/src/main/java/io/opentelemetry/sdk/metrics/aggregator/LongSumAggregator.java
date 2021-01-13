/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.data.LongPoint;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;

final class LongSumAggregator implements Aggregator<Long> {
  private static final LongSumAggregator INSTANCE = new LongSumAggregator();

  /**
   * Returns the instance of this {@link Aggregator}.
   *
   * @return the instance of this {@link Aggregator}.
   */
  static Aggregator<Long> getInstance() {
    return INSTANCE;
  }

  private LongSumAggregator() {}

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
    return a1 + a2;
  }

  @Override
  public MetricData toMetricData(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      InstrumentDescriptor descriptor,
      Map<Labels, Long> accumulationByLabels,
      long startEpochNanos,
      long epochNanos) {
    List<LongPoint> points =
        MetricDataUtils.toLongPointList(accumulationByLabels, startEpochNanos, epochNanos);
    boolean isMonotonic =
        descriptor.getType() == InstrumentType.COUNTER
            || descriptor.getType() == InstrumentType.SUM_OBSERVER;
    return MetricDataUtils.toLongSumMetricData(
        resource, instrumentationLibraryInfo, descriptor, points, isMonotonic);
  }

  static final class Handle extends AggregatorHandle<Long> {
    private final LongAdder current = new LongAdder();

    @Override
    protected Long doAccumulateThenReset() {
      return this.current.sumThenReset();
    }

    @Override
    public void doRecordLong(long value) {
      current.add(value);
    }
  }
}
