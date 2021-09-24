/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.exemplar.ExemplarSampler;
import io.opentelemetry.sdk.metrics.internal.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.metrics.internal.export.CollectionHandle;
import io.opentelemetry.sdk.metrics.internal.view.AttributesProcessor;
import io.opentelemetry.sdk.metrics.view.View;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;

/**
 * Stores aggregated {@link MetricData} for synchronous instruments.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class SynchronousMetricStorage<T> implements MetricStorage, WriteableMetricStorage {
  private final MetricDescriptor metricDescriptor;
  private final DeltaMetricStorage<T> deltaMetricStorage;
  private final TemporalMetricStorage<T> temporalMetricStorage;
  private final AttributesProcessor attributesProcessor;

  /** Constructs metric storage for a given synchronous instrument and view. */
  public static <T> SynchronousMetricStorage<T> create(
      View view,
      InstrumentDescriptor instrumentDescriptor,
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      long startEpochNanos,
      ExemplarSampler sampler) {
    final MetricDescriptor metricDescriptor = MetricDescriptor.create(view, instrumentDescriptor);
    final Aggregator<T> aggregator =
        view.getAggregation()
            .config(instrumentDescriptor)
            .create(
                resource,
                instrumentationLibraryInfo,
                instrumentDescriptor,
                metricDescriptor,
                () -> sampler.createReservoir(view.getAggregation()));
    return new SynchronousMetricStorage<>(
        metricDescriptor, aggregator, view.getAttributesProcessor());
  }

  SynchronousMetricStorage(
      MetricDescriptor metricDescriptor,
      Aggregator<T> aggregator,
      AttributesProcessor attributesProcessor) {
    this.attributesProcessor = attributesProcessor;
    this.metricDescriptor = metricDescriptor;
    this.deltaMetricStorage = new DeltaMetricStorage<>(aggregator);
    this.temporalMetricStorage = new TemporalMetricStorage<>(aggregator, /* isSynchronous= */ true);
  }

  // This is a storage handle to use when the attributes processor requires
  private final BoundStorageHandle lateBoundStorageHandle =
      new BoundStorageHandle() {
        @Override
        public void release() {}

        @Override
        public void recordLong(long value, Attributes attributes, Context context) {
          SynchronousMetricStorage.this.recordLong(value, attributes, context);
        }

        @Override
        public void recordDouble(double value, Attributes attributes, Context context) {
          SynchronousMetricStorage.this.recordDouble(value, attributes, context);
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
  public MetricDescriptor getMetricDescriptor() {
    return metricDescriptor;
  }

  /** Collects bucketed metrics and resets the underlying storage for the next collection period. */
  @Override
  @Nullable
  public MetricData collectAndReset(
      CollectionHandle collector,
      Set<CollectionHandle> allCollectors,
      long startEpochNanos,
      long epochNanos) {
    Map<Attributes, T> result = deltaMetricStorage.collectFor(collector, allCollectors);
    return temporalMetricStorage.buildMetricFor(collector, result, startEpochNanos, epochNanos);
  }
}
