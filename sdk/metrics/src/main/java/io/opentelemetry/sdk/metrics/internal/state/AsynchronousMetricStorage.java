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
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.common.internal.ThrottlingLogger;
import io.opentelemetry.sdk.metrics.View;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.PointData;
import io.opentelemetry.sdk.metrics.internal.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.internal.aggregator.AggregatorFactory;
import io.opentelemetry.sdk.metrics.internal.aggregator.AggregatorHandle;
import io.opentelemetry.sdk.metrics.internal.aggregator.EmptyMetricData;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarFilterInternal;
import io.opentelemetry.sdk.metrics.internal.export.RegisteredReader;
import io.opentelemetry.sdk.metrics.internal.view.AttributesProcessor;
import io.opentelemetry.sdk.metrics.internal.view.RegisteredView;
import io.opentelemetry.sdk.resources.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/**
 * Stores aggregated {@link MetricData} for asynchronous instruments.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public abstract class AsynchronousMetricStorage<T extends PointData> implements MetricStorage {
  private static final Logger logger = Logger.getLogger(AsynchronousMetricStorage.class.getName());

  private final ThrottlingLogger throttlingLogger = new ThrottlingLogger(logger);
  protected final RegisteredReader registeredReader;
  private final MetricDescriptor metricDescriptor;
  private final AggregationTemporality aggregationTemporality;
  protected final Aggregator<T> aggregator;
  private final AttributesProcessor attributesProcessor;
  protected final long instrumentCreationEpochNanos;

  protected final MemoryMode memoryMode;

  /**
   * This field is set to 1 less than the actual intended cardinality limit, allowing the last slot
   * to be filled by the {@link MetricStorage#CARDINALITY_OVERFLOW} series.
   */
  private final int maxCardinality;

  // Handles responsible for aggregating data recorded during callbacks
  protected final Map<Attributes, AggregatorHandle<T>> aggregatorHandles;

  protected final List<T> reusablePointsList = new ArrayList<>();

  private volatile boolean enabled;

  private AsynchronousMetricStorage(
      RegisteredReader registeredReader,
      MetricDescriptor metricDescriptor,
      AggregationTemporality aggregationTemporality,
      Aggregator<T> aggregator,
      AttributesProcessor attributesProcessor,
      int maxCardinality,
      Clock clock,
      boolean enabled) {
    this.registeredReader = registeredReader;
    this.metricDescriptor = metricDescriptor;
    this.aggregationTemporality = aggregationTemporality;
    this.memoryMode = registeredReader.getReader().getMemoryMode();
    this.aggregator = aggregator;
    this.attributesProcessor = attributesProcessor;
    this.instrumentCreationEpochNanos = clock.now();
    this.maxCardinality = maxCardinality - 1;
    this.enabled = enabled;

    // Concurrent hashmap only used to allow for removal during iteration during collection.
    this.aggregatorHandles =
        memoryMode == REUSABLE_DATA ? new PooledHashMap<>() : new ConcurrentHashMap<>();
  }

  /**
   * Create an asynchronous storage instance for the {@link View} and {@link InstrumentDescriptor}.
   */
  // TODO(anuraaga): The cast to generic type here looks suspicious.
  public static <T extends PointData> AsynchronousMetricStorage<T> create(
      RegisteredReader registeredReader,
      RegisteredView registeredView,
      Clock clock,
      InstrumentDescriptor instrumentDescriptor,
      ExemplarFilterInternal exemplarFilter,
      boolean enabled) {
    View view = registeredView.getView();
    MetricDescriptor metricDescriptor =
        MetricDescriptor.create(view, registeredView.getViewSourceInfo(), instrumentDescriptor);
    AggregationTemporality aggregationTemporality =
        registeredReader
            .getReader()
            .getAggregationTemporality(metricDescriptor.getSourceInstrument().getType());
    Aggregator<T> aggregator =
        ((AggregatorFactory) view.getAggregation())
            .createAggregator(
                instrumentDescriptor, exemplarFilter, registeredReader.getReader().getMemoryMode());
    AttributesProcessor attributesProcessor = registeredView.getViewAttributesProcessor();
    int cardinalityLimit = registeredView.getCardinalityLimit();
    return aggregationTemporality == AggregationTemporality.DELTA
        ? new DeltaAsynchronousMetricStorage<>(
            registeredReader,
            metricDescriptor,
            aggregator,
            attributesProcessor,
            cardinalityLimit,
            clock,
            enabled)
        : new CumulativeAsynchronousMetricStorage<>(
            registeredReader,
            metricDescriptor,
            aggregator,
            attributesProcessor,
            cardinalityLimit,
            clock,
            enabled);
  }

  /** Record callback measurement from {@link ObservableLongMeasurement}. */
  void record(Attributes attributes, long value) {
    AggregatorHandle<T> handle = getAggregatorHandle(attributes);
    handle.recordLong(value, attributes, Context.current());
  }

  /** Record callback measurement from {@link ObservableDoubleMeasurement}. */
  void record(Attributes attributes, double value) {
    AggregatorHandle<T> handle = getAggregatorHandle(attributes);
    handle.recordDouble(value, attributes, Context.current());
  }

  private AggregatorHandle<T> getAggregatorHandle(Attributes attributes) {
    Context context = Context.current();
    attributes = attributesProcessor.process(attributes, context);
    AggregatorHandle<T> handle = aggregatorHandles.get(attributes);
    if (handle != null) {
      return handle;
    }
    if (aggregatorHandles.size() >= maxCardinality) {
      throttlingLogger.log(
          Level.WARNING,
          "Instrument "
              + metricDescriptor.getSourceInstrument().getName()
              + " has exceeded the maximum allowed cardinality ("
              + maxCardinality
              + ").");
      attributes = MetricStorage.CARDINALITY_OVERFLOW;
      // Return handle for overflow series, first checking if a handle already exists for it
      handle = aggregatorHandles.get(attributes);
      if (handle != null) {
        return handle;
      }
    }
    // Get handle from pool if available, else create a new one.
    // Note: pooled handles (used only for delta temporality) retain their original
    // creationEpochNanos, but delta storage does not use the handle's creation epoch for the
    // start epoch — it uses the reader's last collect time directly in doCollect(). So the stale
    // creation epoch on a recycled handle does not affect correctness.
    AggregatorHandle<T> newHandle = maybeGetPooledAggregatorHandle();
    if (newHandle == null) {
      newHandle = createAggregatorHandle();
    }
    handle = aggregatorHandles.putIfAbsent(attributes, newHandle);
    return handle != null ? handle : newHandle;
  }

  protected AggregatorHandle<T> createAggregatorHandle() {
    return aggregator.createHandle(
        registeredReader.getLastCollectEpochNanosOrDefault(instrumentCreationEpochNanos));
  }

  @Nullable
  abstract AggregatorHandle<T> maybeGetPooledAggregatorHandle();

  @Override
  public MetricDescriptor getMetricDescriptor() {
    return metricDescriptor;
  }

  /** Returns the registered reader this storage is associated with. */
  public RegisteredReader getRegisteredReader() {
    return registeredReader;
  }

  @Override
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  @Override
  public MetricData collect(
      Resource resource, InstrumentationScopeInfo instrumentationScopeInfo, long epochNanos) {
    Collection<T> result = doCollect(epochNanos);

    return enabled
        ? aggregator.toMetricData(
            resource, instrumentationScopeInfo, metricDescriptor, result, aggregationTemporality)
        : EmptyMetricData.getInstance();
  }

  abstract Collection<T> doCollect(long epochNanos);

  private static final class DeltaAsynchronousMetricStorage<T extends PointData>
      extends AsynchronousMetricStorage<T> {
    // This reference and lastPoints will be swapped at every collection
    private Map<Attributes, T> reusablePointsMap = new PooledHashMap<>();
    private Map<Attributes, T> lastPoints;

    private final ObjectPool<T> reusablePointsPool;
    private final ObjectPool<AggregatorHandle<T>> reusableHandlesPool;

    DeltaAsynchronousMetricStorage(
        RegisteredReader registeredReader,
        MetricDescriptor metricDescriptor,
        Aggregator<T> aggregator,
        AttributesProcessor attributesProcessor,
        int maxCardinality,
        Clock clock,
        boolean enabled) {
      super(
          registeredReader,
          metricDescriptor,
          AggregationTemporality.DELTA,
          aggregator,
          attributesProcessor,
          maxCardinality,
          clock,
          enabled);

      this.reusablePointsPool = new ObjectPool<>(aggregator::createReusablePoint);
      this.reusableHandlesPool = new ObjectPool<>(this::createAggregatorHandle);
      // Concurrent hashmap only used to allow for removal during iteration during collection.
      this.lastPoints =
          memoryMode == REUSABLE_DATA ? new PooledHashMap<>() : new ConcurrentHashMap<>();
    }

    @Nullable
    @Override
    AggregatorHandle<T> maybeGetPooledAggregatorHandle() {
      return reusableHandlesPool.borrowObject();
    }

    @Override
    Collection<T> doCollect(long epochNanos) {
      Map<Attributes, T> currentPoints;
      if (memoryMode == REUSABLE_DATA) {
        // deltaPoints computed in the previous collection can be released
        reusablePointsList.forEach(reusablePointsPool::returnObject);
        reusablePointsList.clear();

        currentPoints = reusablePointsMap;
      } else {
        currentPoints = new HashMap<>();
      }

      // Start time for asynchronous delta instruments is the time of the last collection, or if no
      // collection has yet taken place, the time the instrument was created.
      long startEpochNanos =
          registeredReader.getLastCollectEpochNanosOrDefault(instrumentCreationEpochNanos);

      aggregatorHandles.forEach(
          (attributes, handle) -> {
            T point =
                handle.aggregateThenMaybeReset(
                    startEpochNanos, epochNanos, attributes, /* reset= */ true);

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
            T lastPoint = lastPoints.remove(attributes);

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
        // - If the point was used to compute a delta, it's now in deltaPoints (and thus in
        //  reusablePointsList)
        // - If the point hasn't been used, it's still in lastPoints and can be returned
        lastPoints.forEach((attributes, point) -> reusablePointsPool.returnObject(point));
        lastPoints.clear();

        Map<Attributes, T> tmp = lastPoints;
        lastPoints = reusablePointsMap;
        reusablePointsMap = tmp;
      } else {
        lastPoints = currentPoints;
      }

      aggregatorHandles.forEach(
          (unused, aggregatorHandle) -> reusableHandlesPool.returnObject(aggregatorHandle));
      aggregatorHandles.clear();

      return deltaPoints;
    }
  }

  private static final class CumulativeAsynchronousMetricStorage<T extends PointData>
      extends AsynchronousMetricStorage<T> {
    CumulativeAsynchronousMetricStorage(
        RegisteredReader registeredReader,
        MetricDescriptor metricDescriptor,
        Aggregator<T> aggregator,
        AttributesProcessor attributesProcessor,
        int maxCardinality,
        Clock clock,
        boolean enabled) {
      super(
          registeredReader,
          metricDescriptor,
          AggregationTemporality.CUMULATIVE,
          aggregator,
          attributesProcessor,
          maxCardinality,
          clock,
          enabled);
    }

    @Nullable
    @Override
    AggregatorHandle<T> maybeGetPooledAggregatorHandle() {
      return null;
    }

    @Override
    Collection<T> doCollect(long epochNanos) {
      List<T> currentPoints;
      if (memoryMode == REUSABLE_DATA) {
        // We should not return the points in this list to the pool, they belong to the
        // AggregatorHandle
        reusablePointsList.clear();
        currentPoints = reusablePointsList;
      } else {
        currentPoints = new ArrayList<>();
      }

      // Asynchronous instruments manage their own state. If they stop reporting a measurement for a
      // collection, the series ends. We retain aggregator handles across collections to allow
      // series to report a consistent start time for their lifetime. While collecting, remove any
      // series without measurements this collection.
      // Start time for cumulative asynchronous instruments is:
      // - The instrument creation time if the series first appeared during the first collection
      //   cycle
      // - Otherwise, the preceding collection interval's timestamp
      // This logic is handled in AggregatorHandle creation via #createAggregatorHandle()
      aggregatorHandles.forEach(
          (attributes, handle) -> {
            if (!handle.hasRecordedValues()) {
              aggregatorHandles.remove(attributes);
              return;
            }
            T value =
                handle.aggregateThenMaybeReset(
                    handle.getCreationEpochNanos(), epochNanos, attributes, /* reset= */ true);
            currentPoints.add(value);
          });

      return currentPoints;
    }
  }
}
