/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.metrics.internal.export.CollectionHandle;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Set;
import javax.annotation.Nullable;

final class EmptyMetricStorage implements SynchronousMetricStorage {
  static final EmptyMetricStorage INSTANCE = new EmptyMetricStorage();

  private EmptyMetricStorage() {}

  private final MetricDescriptor descriptor = MetricDescriptor.create("", "", "");
  private final BoundStorageHandle emptyHandle =
      new BoundStorageHandle() {
        @Override
        public void recordLong(long value, Attributes attributes, Context context) {}

        @Override
        public void recordDouble(double value, Attributes attributes, Context context) {}

        @Override
        public void release() {}
      };

  @Override
  public MetricDescriptor getMetricDescriptor() {
    return descriptor;
  }

  @Override
  public BoundStorageHandle bind(Attributes attributes) {
    return emptyHandle;
  }

  @Nullable
  @Override
  public MetricData collectAndReset(
      CollectionHandle collector,
      Set<CollectionHandle> allCollectors,
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      AggregationTemporality temporality,
      long startEpochNanos,
      long epochNanos,
      boolean suppressSynchronousCollection) {
    return null;
  }
}
