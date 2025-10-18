/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.DoubleExemplarData;
import io.opentelemetry.sdk.metrics.data.ExemplarData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.PointData;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarReservoirFactory;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A "null object" Aggregator which denotes no aggregation should occur.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class DropAggregator implements Aggregator<PointData> {

  private static final PointData POINT_DATA =
      new PointData() {
        @Override
        public long getStartEpochNanos() {
          return 0;
        }

        @Override
        public long getEpochNanos() {
          return 0;
        }

        @Override
        public Attributes getAttributes() {
          return Attributes.empty();
        }

        @Override
        public List<? extends ExemplarData> getExemplars() {
          return Collections.emptyList();
        }
      };

  public static final Aggregator<PointData> INSTANCE = new DropAggregator();

  private static final AggregatorHandle<PointData> HANDLE =
      new AggregatorHandle<PointData>(ExemplarReservoirFactory.noSamples()) {
        @Override
        protected boolean isDoubleType() {
          return true;
        }

        @Override
        protected PointData doAggregateThenMaybeResetDoubles(
            long startEpochNanos,
            long epochNanos,
            Attributes attributes,
            List<DoubleExemplarData> exemplars,
            boolean reset) {
          return POINT_DATA;
        }

        @Override
        protected void doRecordLong(long value) {}

        @Override
        protected void doRecordDouble(double value) {}
      };

  private DropAggregator() {}

  @Override
  public AggregatorHandle<PointData> createHandle() {
    return HANDLE;
  }

  @Override
  public MetricData toMetricData(
      Resource resource,
      InstrumentationScopeInfo instrumentationScopeInfo,
      MetricDescriptor metricDescriptor,
      Collection<PointData> points,
      AggregationTemporality temporality) {
    return EmptyMetricData.getInstance();
  }
}
