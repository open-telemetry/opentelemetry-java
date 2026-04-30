/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.PointData;
import io.opentelemetry.sdk.metrics.internal.aggregator.AggregatorHandle;
import io.opentelemetry.sdk.metrics.internal.aggregator.EmptyMetricData;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarReservoirFactory;
import io.opentelemetry.sdk.resources.Resource;
import java.util.function.BiFunction;

final class EmptyMetricStorage implements SynchronousMetricStorage {
  static final EmptyMetricStorage INSTANCE = new EmptyMetricStorage();

  private EmptyMetricStorage() {}

  private final MetricDescriptor descriptor = MetricDescriptor.create("", "", "");

  @Override
  public MetricDescriptor getMetricDescriptor() {
    return descriptor;
  }

  @Override
  public MetricData collect(
      Resource resource, InstrumentationScopeInfo instrumentationScopeInfo, long epochNanos) {
    return EmptyMetricData.getInstance();
  }

  @Override
  public <T> T cachedBind(
      Attributes attributes, BiFunction<AggregatorHandle<?>, Attributes, T> factory) {
    // This storage is disabled; supply a noop handle so recordings are silently dropped.
    AggregatorHandle<PointData> noopHandle =
        new AggregatorHandle<PointData>(0, ExemplarReservoirFactory.noSamples(), true) {
          @Override
          public void recordLong(long value) {}

          @Override
          public void recordDouble(double value) {}

          @Override
          protected void doRecordLong(long value) {}

          @Override
          protected void doRecordDouble(double value) {}
        };
    noopHandle.setAttributes(attributes);
    return factory.apply(noopHandle, attributes);
  }

  @Override
  public void recordLong(long value, Attributes attributes, Context context) {}

  @Override
  public void recordDouble(double value, Attributes attributes, Context context) {}

  @Override
  public boolean isEnabled() {
    return false;
  }

  @Override
  public void setEnabled(boolean enabled) {
    // do nothing
  }
}
