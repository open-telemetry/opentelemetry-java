/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.CollectionRegistration;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.metrics.internal.aggregator.EmptyMetricData;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.metrics.internal.export.RegisteredReader;
import io.opentelemetry.sdk.metrics.internal.view.ViewRegistry;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;

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
  private final MetricReader emptyReader =
      new MetricReader() {
        @Override
        public Aggregation getDefaultAggregation(InstrumentType instrumentType) {
          return Aggregation.drop();
        }

        @Override
        public void register(CollectionRegistration registration) {}

        @Override
        public AggregationTemporality getAggregationTemporality(InstrumentType instrumentType) {
          return AggregationTemporality.CUMULATIVE;
        }

        @Override
        public CompletableResultCode forceFlush() {
          return CompletableResultCode.ofSuccess();
        }

        @Override
        public CompletableResultCode shutdown() {
          return CompletableResultCode.ofFailure();
        }
      };
  private final RegisteredReader registeredReader =
      RegisteredReader.create(
          emptyReader, ViewRegistry.create(emptyReader, Collections.emptyList()));

  @Override
  public MetricDescriptor getMetricDescriptor() {
    return descriptor;
  }

  @Override
  public RegisteredReader getRegisteredReader() {
    return registeredReader;
  }

  @Override
  public BoundStorageHandle bind(Attributes attributes) {
    return emptyHandle;
  }

  @Override
  public MetricData collectAndReset(
      Resource resource,
      InstrumentationScopeInfo instrumentationScopeInfo,
      long startEpochNanos,
      long epochNanos) {
    return EmptyMetricData.getInstance();
  }
}
