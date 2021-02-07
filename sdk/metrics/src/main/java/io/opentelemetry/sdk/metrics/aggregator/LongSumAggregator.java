/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.LongSumData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;

final class LongSumAggregator extends AbstractSumAggregator<Long> {
  LongSumAggregator(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      InstrumentDescriptor descriptor,
      boolean stateful) {
    super(resource, instrumentationLibraryInfo, descriptor, stateful);
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
    return a1 + a2;
  }

  @Override
  public MetricData toMetricData(
      Map<Labels, Long> accumulationByLabels,
      long startEpochNanos,
      long lastCollectionEpoch,
      long epochNanos) {
    InstrumentDescriptor descriptor = getInstrumentDescriptor();
    return MetricData.createLongSum(
        getResource(),
        getInstrumentationLibraryInfo(),
        descriptor.getName(),
        descriptor.getDescription(),
        descriptor.getUnit(),
        LongSumData.create(
            isMonotonic(),
            temporality(),
            MetricDataUtils.toLongPointList(
                accumulationByLabels,
                temporality() == AggregationTemporality.CUMULATIVE
                    ? startEpochNanos
                    : lastCollectionEpoch,
                epochNanos)));
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
