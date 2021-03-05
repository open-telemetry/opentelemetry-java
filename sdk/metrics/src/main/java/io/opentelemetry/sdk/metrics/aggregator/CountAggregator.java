/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import io.opentelemetry.api.metrics.common.Labels;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.LongSumData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
final class CountAggregator extends AbstractAggregator<Long> {
  private final AggregationTemporality temporality;

  CountAggregator(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      InstrumentDescriptor descriptor,
      AggregationTemporality temporality) {
    super(
        resource,
        instrumentationLibraryInfo,
        descriptor,
        temporality == AggregationTemporality.CUMULATIVE);
    this.temporality = temporality;
  }

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
      Map<Labels, Long> accumulationByLabels,
      long startEpochNanos,
      long lastCollectionEpoch,
      long epochNanos) {
    return MetricData.createLongSum(
        getResource(),
        getInstrumentationLibraryInfo(),
        getInstrumentDescriptor().getName(),
        getInstrumentDescriptor().getDescription(),
        "1",
        LongSumData.create(
            /* isMonotonic= */ true,
            temporality,
            MetricDataUtils.toLongPointList(
                accumulationByLabels,
                temporality == AggregationTemporality.CUMULATIVE
                    ? startEpochNanos
                    : lastCollectionEpoch,
                epochNanos)));
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
