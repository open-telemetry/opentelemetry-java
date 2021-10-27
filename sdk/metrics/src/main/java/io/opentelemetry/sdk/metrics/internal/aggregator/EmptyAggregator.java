/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.ExemplarData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.exemplar.ExemplarReservoir;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.resources.Resource;
import java.util.List;
import java.util.Map;

/**
 * A "null object" Aggregator which denotes no aggregation should occur.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class EmptyAggregator implements Aggregator<Void> {

  public static final Aggregator<Void> INSTANCE = new EmptyAggregator();

  private static final AggregatorHandle<Void> HANDLE =
      new AggregatorHandle<Void>(ExemplarReservoir.noSamples()) {
        @Override
        protected void doRecordLong(long value) {}

        @Override
        protected void doRecordDouble(double value) {}

        @Override
        protected Void doAccumulateThenReset(List<ExemplarData> exemplars) {
          return null;
        }
      };

  private EmptyAggregator() {}

  @Override
  public AggregatorHandle<Void> createHandle() {
    return HANDLE;
  }

  @Override
  public Void merge(Void previousAccumulation, Void accumulation) {
    return null;
  }

  @Override
  public Void diff(Void previousAccumulation, Void accumulation) {
    return null;
  }

  @Override
  public MetricData toMetricData(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      MetricDescriptor descriptor,
      Map<Attributes, Void> accumulationByLabels,
      AggregationTemporality temporality,
      long startEpochNanos,
      long lastCollectionEpoch,
      long epochNanos) {
    return null;
  }
}
