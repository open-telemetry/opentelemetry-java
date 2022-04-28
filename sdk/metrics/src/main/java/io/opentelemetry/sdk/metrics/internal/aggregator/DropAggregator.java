/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.DoubleExemplarData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.metrics.internal.exemplar.DoubleExemplarReservoir;
import io.opentelemetry.sdk.resources.Resource;
import java.util.List;
import java.util.Map;

/**
 * A "null object" Aggregator which denotes no aggregation should occur.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class DropAggregator implements Aggregator<Object, DoubleExemplarData> {

  private static final Object ACCUMULATION = new Object();

  public static final Aggregator<Object, DoubleExemplarData> INSTANCE = new DropAggregator();

  private static final AggregatorHandle<Object, DoubleExemplarData> HANDLE =
      new AggregatorHandle<Object, DoubleExemplarData>(DoubleExemplarReservoir.noSamples()) {
        @Override
        protected void doRecordLong(long value) {}

        @Override
        protected void doRecordDouble(double value) {}

        @Override
        protected Object doAccumulateThenReset(List<DoubleExemplarData> exemplars) {
          return ACCUMULATION;
        }
      };

  private DropAggregator() {}

  @Override
  public AggregatorHandle<Object, DoubleExemplarData> createHandle() {
    return HANDLE;
  }

  @Override
  public Object merge(Object previousAccumulation, Object accumulation) {
    return ACCUMULATION;
  }

  @Override
  public Object diff(Object previousAccumulation, Object accumulation) {
    return ACCUMULATION;
  }

  @Override
  public MetricData toMetricData(
      Resource resource,
      InstrumentationScopeInfo instrumentationScopeInfo,
      MetricDescriptor descriptor,
      Map<Attributes, Object> accumulationByLabels,
      AggregationTemporality temporality,
      long startEpochNanos,
      long lastCollectionEpoch,
      long epochNanos) {
    return EmptyMetricData.getInstance();
  }
}
