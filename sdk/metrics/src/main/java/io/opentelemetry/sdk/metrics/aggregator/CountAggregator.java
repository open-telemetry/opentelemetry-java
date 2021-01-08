/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.accumulation.LongAccumulation;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public final class CountAggregator implements Aggregator<LongAccumulation> {
  private static final Aggregator<LongAccumulation> INSTANCE = new CountAggregator();

  /**
   * Returns the instance of this {@link Aggregator}.
   *
   * @return the instance of this {@link Aggregator}.
   */
  public static Aggregator<LongAccumulation> getInstance() {
    return INSTANCE;
  }

  private CountAggregator() {}

  @Override
  public AggregatorHandle<LongAccumulation> createHandle() {
    return new Handle();
  }

  @Override
  public LongAccumulation accumulateDouble(double value) {
    return LongAccumulation.create(1);
  }

  @Override
  public LongAccumulation accumulateLong(long value) {
    return LongAccumulation.create(1);
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

    return MetricData.createLongSum(
        resource,
        instrumentationLibraryInfo,
        descriptor.getName(),
        descriptor.getDescription(),
        "1",
        MetricData.LongSumData.create(
            /* isMonotonic= */ true, MetricData.AggregationTemporality.CUMULATIVE, points));
  }

  static final class Handle extends AggregatorHandle<LongAccumulation> {
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
    protected LongAccumulation doAccumulateThenReset() {
      return LongAccumulation.create(current.sumThenReset());
    }
  }
}
