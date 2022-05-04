/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.ExemplarData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.metrics.internal.export.RegisteredReader;
import io.opentelemetry.sdk.metrics.internal.view.AttributesProcessor;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Map;
import java.util.Objects;

/**
 * Stores aggregated {@link MetricData} for synchronous instruments.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class DefaultSynchronousMetricStorage<T, U extends ExemplarData>
    implements SynchronousMetricStorage {
  private final RegisteredReader registeredReader;
  private final MetricDescriptor metricDescriptor;
  private final AggregationTemporality aggregationTemporality;
  private final DeltaMetricStorage<T, U> deltaMetricStorage;
  private final TemporalMetricStorage<T, U> temporalMetricStorage;
  private final AttributesProcessor attributesProcessor;

  DefaultSynchronousMetricStorage(
      RegisteredReader registeredReader,
      MetricDescriptor metricDescriptor,
      Aggregator<T, U> aggregator,
      AttributesProcessor attributesProcessor) {
    this.registeredReader = registeredReader;
    this.metricDescriptor = metricDescriptor;
    this.aggregationTemporality =
        registeredReader
            .getReader()
            .getAggregationTemporality(metricDescriptor.getSourceInstrument().getType());
    this.deltaMetricStorage =
        new DeltaMetricStorage<>(aggregator, metricDescriptor.getSourceInstrument());
    this.temporalMetricStorage = new TemporalMetricStorage<>(aggregator, /* isSynchronous= */ true);
    this.attributesProcessor = attributesProcessor;
  }

  // This is a storage handle to use when the attributes processor requires
  private final BoundStorageHandle lateBoundStorageHandle =
      new BoundStorageHandle() {
        @Override
        public void release() {}

        @Override
        public void recordLong(long value, Attributes attributes, Context context) {
          DefaultSynchronousMetricStorage.this.recordLong(value, attributes, context);
        }

        @Override
        public void recordDouble(double value, Attributes attributes, Context context) {
          DefaultSynchronousMetricStorage.this.recordDouble(value, attributes, context);
        }
      };

  @Override
  public BoundStorageHandle bind(Attributes attributes) {
    Objects.requireNonNull(attributes, "attributes");
    if (attributesProcessor.usesContext()) {
      // We cannot pre-bind attributes because we need to pull attributes from context.
      return lateBoundStorageHandle;
    }
    return deltaMetricStorage.bind(attributesProcessor.process(attributes, Context.current()));
  }

  // Overridden to make sure attributes processor can pull baggage.
  @Override
  public void recordLong(long value, Attributes attributes, Context context) {
    Objects.requireNonNull(attributes, "attributes");
    attributes = attributesProcessor.process(attributes, context);
    BoundStorageHandle handle = deltaMetricStorage.bind(attributes);
    try {
      handle.recordLong(value, attributes, context);
    } finally {
      handle.release();
    }
  }

  // Overridden to make sure attributes processor can pull baggage.
  @Override
  public void recordDouble(double value, Attributes attributes, Context context) {
    Objects.requireNonNull(attributes, "attributes");
    attributes = attributesProcessor.process(attributes, context);
    BoundStorageHandle handle = deltaMetricStorage.bind(attributes);
    try {
      handle.recordDouble(value, attributes, context);
    } finally {
      handle.release();
    }
  }

  @Override
  public MetricData collectAndReset(
      Resource resource,
      InstrumentationScopeInfo instrumentationScopeInfo,
      long startEpochNanos,
      long epochNanos) {
    Map<Attributes, T> result = deltaMetricStorage.collect();
    return temporalMetricStorage.buildMetricFor(
        registeredReader,
        resource,
        instrumentationScopeInfo,
        getMetricDescriptor(),
        aggregationTemporality,
        result,
        startEpochNanos,
        epochNanos);
  }

  @Override
  public MetricDescriptor getMetricDescriptor() {
    return metricDescriptor;
  }

  @Override
  public RegisteredReader getRegisteredReader() {
    return registeredReader;
  }
}
