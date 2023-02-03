/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.internal.ThrottlingLogger;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.ExemplarData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.PointData;
import io.opentelemetry.sdk.metrics.internal.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.internal.aggregator.AggregatorHandle;
import io.opentelemetry.sdk.metrics.internal.aggregator.EmptyMetricData;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.metrics.internal.export.RegisteredReader;
import io.opentelemetry.sdk.metrics.internal.view.AttributesProcessor;
import io.opentelemetry.sdk.resources.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/**
 * Stores aggregated {@link MetricData} for synchronous instruments.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class DefaultSynchronousMetricStorage<T extends PointData, U extends ExemplarData>
    implements SynchronousMetricStorage {

  private static final Logger internalLogger =
      Logger.getLogger(DefaultSynchronousMetricStorage.class.getName());

  private final ThrottlingLogger logger = new ThrottlingLogger(internalLogger);
  private final RegisteredReader registeredReader;
  private final MetricDescriptor metricDescriptor;
  private final AggregationTemporality aggregationTemporality;
  private final Aggregator<T, U> aggregator;
  private final ConcurrentHashMap<Attributes, AggregatorHandle<T, U>> aggregatorHandles =
      new ConcurrentHashMap<>();
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
    this.attributesProcessor = attributesProcessor;
  }

  @Override
  public void recordLong(long value, Attributes attributes, Context context) {
    AggregatorHandle<T, U> handle = getAggregatorHandle(attributes, context);
    if (handle != null) {
      handle.recordLong(value, attributes, context);
    }
  }

  @Override
  public void recordDouble(double value, Attributes attributes, Context context) {
    AggregatorHandle<T, U> handle = getAggregatorHandle(attributes, context);
    if (handle != null) {
      handle.recordDouble(value, attributes, context);
    }
  }

  @Nullable
  private AggregatorHandle<T, U> getAggregatorHandle(Attributes attributes, Context context) {
    Objects.requireNonNull(attributes, "attributes");
    attributes = attributesProcessor.process(attributes, context);
    AggregatorHandle<T, U> handle = aggregatorHandles.get(attributes);
    if (handle != null) {
      return handle;
    }
    if (aggregatorHandles.size() >= MAX_CARDINALITY) {
      logger.log(
          Level.WARNING,
          "Instrument "
              + metricDescriptor.getSourceInstrument().getName()
              + " has exceeded the maximum allowed cardinality ("
              + MAX_CARDINALITY
              + ").");
      return null;
    }
    AggregatorHandle<T, U> newHandle = aggregator.createHandle();
    handle = aggregatorHandles.putIfAbsent(attributes, newHandle);
    return handle != null ? handle : newHandle;
  }

  @Override
  public MetricData collect(
      Resource resource,
      InstrumentationScopeInfo instrumentationScopeInfo,
      long startEpochNanos,
      long epochNanos) {
    boolean reset = aggregationTemporality == AggregationTemporality.DELTA;
    long start =
        aggregationTemporality == AggregationTemporality.DELTA
            ? registeredReader.getLastCollectEpochNanos()
            : startEpochNanos;

    // Grab aggregated points.
    List<T> points = new ArrayList<>(aggregatorHandles.size());
    for (Map.Entry<Attributes, AggregatorHandle<T, U>> entry : aggregatorHandles.entrySet()) {
      if (reset) {
        aggregatorHandles.remove(entry.getKey(), entry.getValue());
      }
      T point = entry.getValue().aggregateThenMaybeReset(start, epochNanos, entry.getKey(), reset);
      if (point == null) {
        continue;
      }
      points.add(point);
    }

    if (points.isEmpty()) {
      return EmptyMetricData.getInstance();
    }

    return aggregator.toMetricData(
        resource, instrumentationScopeInfo, metricDescriptor, points, aggregationTemporality);
  }

  @Override
  public MetricDescriptor getMetricDescriptor() {
    return metricDescriptor;
  }
}
