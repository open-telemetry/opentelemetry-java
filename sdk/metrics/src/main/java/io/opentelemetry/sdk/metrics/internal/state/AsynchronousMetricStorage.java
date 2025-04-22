/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import static io.opentelemetry.sdk.common.export.MemoryMode.REUSABLE_DATA;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.ObservableDoubleMeasurement;
import io.opentelemetry.api.metrics.ObservableLongMeasurement;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.internal.ThrottlingLogger;
import io.opentelemetry.sdk.metrics.View;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.ExemplarData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.PointData;
import io.opentelemetry.sdk.metrics.internal.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.internal.aggregator.AggregatorFactory;
import io.opentelemetry.sdk.metrics.internal.aggregator.AggregatorHandle;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarFilter;
import io.opentelemetry.sdk.metrics.internal.export.RegisteredReader;
import io.opentelemetry.sdk.metrics.internal.view.AttributesProcessor;
import io.opentelemetry.sdk.metrics.internal.view.RegisteredView;
import io.opentelemetry.sdk.resources.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Stores aggregated {@link MetricData} for asynchronous instruments.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class AsynchronousMetricStorage<T extends PointData, U extends ExemplarData>
    implements MetricStorage {
  private static final Logger logger = Logger.getLogger(AsynchronousMetricStorage.class.getName());

  private final ThrottlingLogger throttlingLogger = new ThrottlingLogger(logger);
  private final RegisteredReader registeredReader;
  private final MetricDescriptor metricDescriptor;
  private final AggregationTemporality aggregationTemporality;
  private final Aggregator<T, U> aggregator;
  private final AttributesProcessor attributesProcessor;
  private final MemoryMode memoryMode;

  /**
   * This field is set to 1 less than the actual intended cardinality limit, allowing the last slot
   * to be filled by the {@link MetricStorage#CARDINALITY_OVERFLOW} series.
   */
  private final int maxCardinality;

  // Handles responsible for aggregating data recorded during callbacks
  private final Map<Attributes, AggregatorHandle<T, U>> aggregatorHandles;

  // Only populated if aggregationTemporality == DELTA
  private Map<Attributes, T> lastPoints;

  // Only populated if memoryMode == REUSABLE_DATA
  private final ObjectPool<T> reusablePointsPool;
  private final List<T> reusablePointsList = new ArrayList<>();
  // If aggregationTemporality == DELTA, this reference and lastPoints will be swapped at every
  // collection
  private Map<Attributes, T> reusablePointsMap = new PooledHashMap<>();

  // Time information relative to recording of data in aggregatorHandles, set while calling
  // callbacks
  private long startEpochNanos;
  private long epochNanos;

  // Delete the first empty handle to reclaim space, and return quickly for all subsequent entries
  private final BiConsumer<Attributes, AggregatorHandle<T, U>> handlesDeleter =
      new BiConsumer<Attributes, AggregatorHandle<T, U>>() {
        private boolean active = true;

        @Override
        public void accept(Attributes attributes, AggregatorHandle<T, U> handle) {
          if (active && !handle.hasRecordedValues()) {
            aggregatorHandles.remove(attributes);
            active = false;
          }
        }
      };

  private AsynchronousMetricStorage(
      RegisteredReader registeredReader,
      MetricDescriptor metricDescriptor,
      Aggregator<T, U> aggregator,
      AttributesProcessor attributesProcessor,
      int maxCardinality) {
    this.registeredReader = registeredReader;
    this.metricDescriptor = metricDescriptor;
    this.aggregationTemporality =
        registeredReader
            .getReader()
            .getAggregationTemporality(metricDescriptor.getSourceInstrument().getType());
    this.memoryMode = registeredReader.getReader().getMemoryMode();
    this.aggregator = aggregator;
    this.attributesProcessor = attributesProcessor;
    this.maxCardinality = maxCardinality - 1;
    this.reusablePointsPool = new ObjectPool<>(aggregator::createReusablePoint);

    if (memoryMode == REUSABLE_DATA) {
      this.lastPoints = new PooledHashMap<>();
      this.aggregatorHandles = new PooledHashMap<>();
    } else {
      this.lastPoints = new HashMap<>();
      this.aggregatorHandles = new HashMap<>();
    }
  }

  /**
   * Create an asynchronous storage instance for the {@link View} and {@link InstrumentDescriptor}.
   */
  // TODO(anuraaga): The cast to generic type here looks suspicious.
  public static <T extends PointData, U extends ExemplarData>
      AsynchronousMetricStorage<T, U> create(
          RegisteredReader registeredReader,
          RegisteredView registeredView,
          InstrumentDescriptor instrumentDescriptor) {
    View view = registeredView.getView();
    MetricDescriptor metricDescriptor =
        MetricDescriptor.create(view, registeredView.getViewSourceInfo(), instrumentDescriptor);
    Aggregator<T, U> aggregator =
        ((AggregatorFactory) view.getAggregation())
            .createAggregator(
                instrumentDescriptor,
                ExemplarFilter.alwaysOff(),
                registeredReader.getReader().getMemoryMode());
    return new AsynchronousMetricStorage<>(
        registeredReader,
        metricDescriptor,
        aggregator,
        registeredView.getViewAttributesProcessor(),
        registeredView.getCardinalityLimit());
  }

  /** Record callback measurement from {@link ObservableLongMeasurement}. */
  void record(Attributes attributes, long value) {
    attributes = validateAndProcessAttributes(attributes);
    AggregatorHandle<T, U> handle =
        aggregatorHandles.computeIfAbsent(attributes, key -> aggregator.createHandle());
    handle.recordLong(value, attributes, Context.current());
  }

  /** Record callback measurement from {@link ObservableDoubleMeasurement}. */
  void record(Attributes attributes, double value) {
    attributes = validateAndProcessAttributes(attributes);
    AggregatorHandle<T, U> handle =
        aggregatorHandles.computeIfAbsent(attributes, key -> aggregator.createHandle());
    handle.recordDouble(value, attributes, Context.current());
  }

  void setEpochInformation(long startEpochNanos, long epochNanos) {
    this.startEpochNanos =
        aggregationTemporality == AggregationTemporality.DELTA
            ? registeredReader.getLastCollectEpochNanos()
            : startEpochNanos;
    this.epochNanos = epochNanos;
  }

  private Attributes validateAndProcessAttributes(Attributes attributes) {
    Context context = Context.current();
    attributes = attributesProcessor.process(attributes, context);

    if (aggregatorHandles.size() == maxCardinality) {
      aggregatorHandles.forEach(handlesDeleter);
      if (aggregatorHandles.size() == maxCardinality) {
        throttlingLogger.log(
            Level.WARNING,
            "Instrument "
                + metricDescriptor.getSourceInstrument().getName()
                + " has exceeded the maximum allowed cardinality ("
                + maxCardinality
                + ").");
        return MetricStorage.CARDINALITY_OVERFLOW;
      }
    }
    return attributes;
  }

  @Override
  public MetricDescriptor getMetricDescriptor() {
    return metricDescriptor;
  }

  /** Returns the registered reader this storage is associated with. */
  public RegisteredReader getRegisteredReader() {
    return registeredReader;
  }

  @Override
  public MetricData collect(
      Resource resource,
      InstrumentationScopeInfo instrumentationScopeInfo,
      long startEpochNanos,
      long epochNanos) {
    Collection<T> result =
        aggregationTemporality == AggregationTemporality.DELTA
            ? collectWithDeltaAggregationTemporality()
            : collectWithCumulativeAggregationTemporality();

    if (memoryMode != REUSABLE_DATA) {
      aggregatorHandles.clear();
    }

    return aggregator.toMetricData(
        resource, instrumentationScopeInfo, metricDescriptor, result, aggregationTemporality);
  }

  private Collection<T> collectWithDeltaAggregationTemporality() {
    Map<Attributes, T> currentPoints;
    if (memoryMode == REUSABLE_DATA) {
      // deltaPoints computed in the previous collection can be released
      reusablePointsList.forEach(reusablePointsPool::returnObject);
      reusablePointsList.clear();

      currentPoints = reusablePointsMap;
    } else {
      currentPoints = new HashMap<>();
    }

    aggregatorHandles.forEach(
        (attributes, handle) -> {
          if (!handle.hasRecordedValues()) {
            return;
          }

          T point =
              handle.aggregateThenMaybeReset(
                  AsynchronousMetricStorage.this.startEpochNanos,
                  AsynchronousMetricStorage.this.epochNanos,
                  attributes,
                  /* reset= */ true);

          T pointForCurrentPoints;
          if (memoryMode == REUSABLE_DATA) {
            // AggregatorHandle is going to modify the point eventually, but we must persist its
            // value to used it at the next collection (within lastPoints). Thus, we make a copy.
            pointForCurrentPoints = reusablePointsPool.borrowObject();
            aggregator.copyPoint(point, pointForCurrentPoints);
          } else {
            pointForCurrentPoints = point;
          }
          currentPoints.put(attributes, pointForCurrentPoints);
        });

    List<T> deltaPoints = memoryMode == REUSABLE_DATA ? reusablePointsList : new ArrayList<>();
    currentPoints.forEach(
        (attributes, currentPoint) -> {
          T lastPoint = lastPoints.get(attributes);

          T deltaPoint;
          if (lastPoint == null) {
            if (memoryMode == REUSABLE_DATA) {
              // All deltaPoints are released at the end of the collection. Thus, we need a copy
              // to make sure currentPoint can still be used within lastPoints during the next
              // collection.
              deltaPoint = reusablePointsPool.borrowObject();
              aggregator.copyPoint(currentPoint, deltaPoint);
            } else {
              deltaPoint = currentPoint;
            }
          } else {
            if (memoryMode == REUSABLE_DATA) {
              aggregator.diffInPlace(lastPoint, currentPoint);
              deltaPoint = lastPoint;
            } else {
              deltaPoint = aggregator.diff(lastPoint, currentPoint);
            }
          }
          deltaPoints.add(deltaPoint);
        });

    if (memoryMode == REUSABLE_DATA) {
      // lastPoints for the current collection can be discarded when the collection is completed.
      // They can be returned to the pool because they're not managed by the AggregatorHandle,
      // we made a copy.
      lastPoints.forEach((attributes, point) -> reusablePointsPool.returnObject(point));
      lastPoints.clear();

      Map<Attributes, T> tmp = lastPoints;
      lastPoints = reusablePointsMap;
      reusablePointsMap = tmp;
    } else {
      lastPoints = currentPoints;
    }

    return deltaPoints;
  }

  private Collection<T> collectWithCumulativeAggregationTemporality() {
    List<T> currentPoints;
    if (memoryMode == REUSABLE_DATA) {
      // We should not return the points in this list to the pool, they belong to the
      // AggregatorHandle
      reusablePointsList.clear();
      currentPoints = reusablePointsList;
    } else {
      currentPoints = new ArrayList<>();
    }

    aggregatorHandles.forEach(
        (attributes, handle) -> {
          if (!handle.hasRecordedValues()) {
            return;
          }

          T value =
              handle.aggregateThenMaybeReset(
                  AsynchronousMetricStorage.this.startEpochNanos,
                  AsynchronousMetricStorage.this.epochNanos,
                  attributes,
                  /* reset= */ true);
          currentPoints.add(value);
        });
    return currentPoints;
  }

  @Override
  public boolean isEmpty() {
    return aggregator == Aggregator.drop();
  }
}
