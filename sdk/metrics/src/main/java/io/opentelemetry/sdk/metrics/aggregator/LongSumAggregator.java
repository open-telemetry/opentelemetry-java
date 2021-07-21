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

final class LongSumAggregator extends AbstractSumAggregator<Long> {

  LongSumAggregator(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      InstrumentDescriptor descriptor,
      AggregationTemporality temporality) {
    super(resource, instrumentationLibraryInfo, descriptor, temporality);
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
  Long mergeSum(Long previousAccumulation, Long accumulation) {
    return previousAccumulation + accumulation;
  }

  @Override
  Long mergeDiff(Long previousAccumulation, Long accumulation) {
    return accumulation - previousAccumulation;
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
