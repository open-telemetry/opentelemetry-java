/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import static io.opentelemetry.sdk.metrics.internal.state.MetricStorageUtils.MAX_ACCUMULATIONS;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.internal.ThrottlingLogger;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.ExemplarData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.internal.aggregator.AggregatorHandle;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.metrics.internal.export.RegisteredReader;
import io.opentelemetry.sdk.metrics.internal.view.AttributesProcessor;
import io.opentelemetry.sdk.resources.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Stores aggregated {@link MetricData} for synchronous instruments.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class DefaultSynchronousMetricStorage<T, U extends ExemplarData>
    implements SynchronousMetricStorage {

  private static final ThrottlingLogger logger =
      new ThrottlingLogger(Logger.getLogger(DefaultSynchronousMetricStorage.class.getName()));
  private static final BoundStorageHandle NOOP_STORAGE_HANDLE = new NoopBoundHandle();

  private final RegisteredReader registeredReader;
  private final MetricDescriptor metricDescriptor;
  private final AggregationTemporality aggregationTemporality;
  private final Aggregator<T, U> aggregator;
  private final ConcurrentHashMap<Attributes, AggregatorHandle<T, U>> activeCollectionStorage =
      new ConcurrentHashMap<>();
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
    this.aggregator = aggregator;
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
    return doBind(attributesProcessor.process(attributes, Context.current()));
  }

  private BoundStorageHandle doBind(Attributes attributes) {
    AggregatorHandle<T, U> aggregatorHandle = activeCollectionStorage.get(attributes);
    if (aggregatorHandle != null && aggregatorHandle.acquire()) {
      // At this moment it is guaranteed that the Bound is in the map and will not be removed.
      return aggregatorHandle;
    }

    // Missing entry or no longer mapped. Try to add a new one if not exceeded cardinality limits.
    aggregatorHandle = aggregator.createHandle();
    while (true) {
      if (activeCollectionStorage.size() >= MAX_ACCUMULATIONS) {
        logger.log(
            Level.WARNING,
            "Instrument "
                + metricDescriptor.getSourceInstrument().getName()
                + " has exceeded the maximum allowed accumulations ("
                + MAX_ACCUMULATIONS
                + ").");
        return NOOP_STORAGE_HANDLE;
      }
      AggregatorHandle<T, U> boundAggregatorHandle =
          activeCollectionStorage.putIfAbsent(attributes, aggregatorHandle);
      if (boundAggregatorHandle != null) {
        if (boundAggregatorHandle.acquire()) {
          // At this moment it is guaranteed that the Bound is in the map and will not be removed.
          return boundAggregatorHandle;
        }
        // Try to remove the boundAggregator. This will race with the collect method, but only one
        // will succeed.
        activeCollectionStorage.remove(attributes, boundAggregatorHandle);
        continue;
      }
      return aggregatorHandle;
    }
  }

  // Overridden to make sure attributes processor can pull baggage.
  @Override
  public void recordLong(long value, Attributes attributes, Context context) {
    Objects.requireNonNull(attributes, "attributes");
    attributes = attributesProcessor.process(attributes, context);
    BoundStorageHandle handle = doBind(attributes);
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
    BoundStorageHandle handle = doBind(attributes);
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
    // Grab accumulated measurements.
    Map<Attributes, T> accumulations = new HashMap<>();
    for (Map.Entry<Attributes, AggregatorHandle<T, U>> entry : activeCollectionStorage.entrySet()) {
      boolean unmappedEntry = entry.getValue().tryUnmap();
      if (unmappedEntry) {
        // If able to unmap then remove the record from the current Map. This can race with the
        // acquire but because we requested a specific value only one will succeed.
        activeCollectionStorage.remove(entry.getKey(), entry.getValue());
      }
      T accumulation = entry.getValue().accumulateThenReset(entry.getKey());
      if (accumulation == null) {
        continue;
      }
      accumulations.put(entry.getKey(), accumulation);
    }

    return temporalMetricStorage.buildMetricFor(
        registeredReader,
        resource,
        instrumentationScopeInfo,
        getMetricDescriptor(),
        aggregationTemporality,
        accumulations,
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

  /** An implementation of {@link BoundStorageHandle} that does not record. */
  private static class NoopBoundHandle implements BoundStorageHandle {

    @Override
    public void recordLong(long value, Attributes attributes, Context context) {}

    @Override
    public void recordDouble(double value, Attributes attributes, Context context) {}

    @Override
    public void release() {}
  }
}
