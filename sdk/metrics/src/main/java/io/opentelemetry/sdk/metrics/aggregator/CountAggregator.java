/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.LongPoint;
import io.opentelemetry.sdk.metrics.data.LongSumData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
final class CountAggregator implements Aggregator<Long> {
  private static final Aggregator<Long> INSTANCE = new CountAggregator();

  /**
   * Returns the instance of this {@link Aggregator}.
   *
   * @return the instance of this {@link Aggregator}.
   */
  static Aggregator<Long> getInstance() {
    return INSTANCE;
  }

  private CountAggregator() {}

  @Override
  public AggregatorHandle<Long> createHandle() {
    return new Handle();
  }

  @Override
  public Long accumulateDouble(double value) {
    return 1L;
  }

  @Override
  public Long accumulateLong(long value) {
    return 1L;
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

    return MetricData.createLongSum(
        resource,
        instrumentationLibraryInfo,
        descriptor.getName(),
        descriptor.getDescription(),
        "1",
        LongSumData.create(/* isMonotonic= */ true, AggregationTemporality.CUMULATIVE, points));
  }

  static final class Handle extends AggregatorHandle<Long> {
    private final LongAdder current = new LongAdder();

    private Handle() {}

    @Override
    protected void doRecordLong(long value) {
      current.add(1);
    }

    @Override
    protected void doRecordDouble(double value) {
      current.add(1);
    }

    @Override
    protected Long doAccumulateThenReset() {
      return current.sumThenReset();
    }
  }
}
