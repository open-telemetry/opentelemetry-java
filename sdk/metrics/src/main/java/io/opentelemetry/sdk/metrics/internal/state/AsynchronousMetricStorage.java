/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.ObservableDoubleMeasurement;
import io.opentelemetry.api.metrics.ObservableLongMeasurement;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.internal.ThrottlingLogger;
import io.opentelemetry.sdk.metrics.View;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.ExemplarData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.PointData;
import io.opentelemetry.sdk.metrics.export.MemoryModeSelector.MemoryMode;
import io.opentelemetry.sdk.metrics.internal.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.internal.aggregator.AggregatorFactory;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarFilter;
import io.opentelemetry.sdk.metrics.internal.export.RegisteredReader;
import io.opentelemetry.sdk.metrics.internal.view.AttributesProcessor;
import io.opentelemetry.sdk.metrics.internal.view.RegisteredView;
import io.opentelemetry.sdk.resources.Resource;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static io.opentelemetry.sdk.metrics.export.MemoryModeSelector.MemoryMode.REUSABLE_DATA;

/**
 * Stores aggregated {@link MetricData} for asynchronous instruments.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
final class AsynchronousMetricStorage<T extends PointData, U extends ExemplarData>
    implements MetricStorage {
  private static final Logger logger = Logger.getLogger(AsynchronousMetricStorage.class.getName());

  private final ThrottlingLogger throttlingLogger = new ThrottlingLogger(logger);
  private final RegisteredReader registeredReader;
  private final MetricDescriptor metricDescriptor;
  private final AggregationTemporality aggregationTemporality;
  private final Aggregator<T, U> aggregator;
  private final AttributesProcessor attributesProcessor;
  private final int maxCardinality;
  private Map<Attributes, T> points;
  private final MemoryMode memoryMode;

  // Only populated if memoryMode == REUSABLE_DATA
  private final ObjectPool<T> reusablePointsPool;

  // Only populated if memoryMode == REUSABLE_DATA
  @Nullable
  private PooledHashMap<Attributes, T> previousCollectResult = null;
  @Nullable
  private ArrayList<T> previousReturnedValues = null;

  // Only populated if aggregationTemporality == DELTA
  private Map<Attributes, T> lastPoints;


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
    this.memoryMode =
        registeredReader
            .getReader()
            .getMemoryMode();
    this.aggregator = aggregator;
    this.attributesProcessor = attributesProcessor;
    this.maxCardinality = maxCardinality;
    this.reusablePointsPool = new ObjectPool<>(aggregator::createReusablePoint);
    if (memoryMode == REUSABLE_DATA) {
      lastPoints = new PooledHashMap<>();
      points = new PooledHashMap<>();
    } else {
      lastPoints = new HashMap<>();
      points = new HashMap<>();
    }
  }

  /**
   * Create an asynchronous storage instance for the {@link View} and {@link InstrumentDescriptor}.
   */
  // TODO(anuraaga): The cast to generic type here looks suspicious.
  static <T extends PointData, U extends ExemplarData> AsynchronousMetricStorage<T, U> create(
      RegisteredReader registeredReader,
      RegisteredView registeredView,
      InstrumentDescriptor instrumentDescriptor) {
    View view = registeredView.getView();
    MetricDescriptor metricDescriptor =
        MetricDescriptor.create(view, registeredView.getViewSourceInfo(), instrumentDescriptor);
    Aggregator<T, U> aggregator =
        ((AggregatorFactory) view.getAggregation())
            .createAggregator(instrumentDescriptor, ExemplarFilter.alwaysOff());
    return new AsynchronousMetricStorage<>(
        registeredReader,
        metricDescriptor,
        aggregator,
        registeredView.getViewAttributesProcessor(),
        registeredView.getCardinalityLimit());
  }

  /**
   * Record callback measurement from {@link ObservableLongMeasurement} or {@link
   * ObservableDoubleMeasurement}.
   */
  void record(Measurement measurement) {
    Context context = Context.current();
    Attributes processedAttributes = attributesProcessor.process(measurement.attributes(), context);
    long start =
        aggregationTemporality == AggregationTemporality.DELTA
            ? registeredReader.getLastCollectEpochNanos()
            : measurement.startEpochNanos();

    if (measurement instanceof LeasedMeasurement) {
      if (measurement.hasDoubleValue()) {
        LeasedMeasurement.setDoubleMeasurement(
            (LeasedMeasurement) measurement,
            start,
            measurement.epochNanos(),
            measurement.doubleValue(),
            processedAttributes);
      } else {
        LeasedMeasurement.setLongMeasurement(
            (LeasedMeasurement) measurement,
            start,
            measurement.epochNanos(),
            measurement.longValue(),
            processedAttributes);
      }
    } else {
      measurement =
          measurement.hasDoubleValue()
              ? ImmutableMeasurement.doubleMeasurement(
              start, measurement.epochNanos(), measurement.doubleValue(), processedAttributes)
              : ImmutableMeasurement.longMeasurement(
                  start, measurement.epochNanos(), measurement.longValue(), processedAttributes);
    }

    switch (memoryMode) {
      case REUSABLE_DATA:
        T reusableDataPoint = reusablePointsPool.borrowObject();
        aggregator.toPoint(measurement, reusableDataPoint);
        recordPoint(reusableDataPoint);
        break;
      case IMMUTABLE_DATA:
        recordPoint(aggregator.toPoint(measurement));
        break;
    }
  }

  private void recordPoint(T point) {
    Attributes attributes = point.getAttributes();

    if (points.size() >= maxCardinality) {
      throttlingLogger.log(
          Level.WARNING,
          "Instrument "
              + metricDescriptor.getSourceInstrument().getName()
              + " has exceeded the maximum allowed cardinality ("
              + maxCardinality
              + ").");
      return;
    }

    // Check there is not already a recording for the attributes
    if (points.containsKey(attributes)) {
      throttlingLogger.log(
          Level.WARNING,
          "Instrument "
              + metricDescriptor.getSourceInstrument().getName()
              + " has recorded multiple values for the same attributes: "
              + attributes);
      return;
    }

    points.put(attributes, point);
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
    if (memoryMode == REUSABLE_DATA) {
      // Collect can not run concurrently for same reader, hence we safely assume
      // the previous collect result has been used and done with
      if (previousCollectResult != null) {
        previousCollectResult.forEach( (k, v) -> reusablePointsPool.returnObject(v));
        previousCollectResult.clear();
      }
    }
    Map<Attributes, T> result;
    if (aggregationTemporality == AggregationTemporality.DELTA) {
      Map<Attributes, T> points = this.points;
      Map<Attributes, T> lastPoints = this.lastPoints;

      lastPoints.entrySet().removeIf(entry -> {
        boolean pointsContainsKey = points.containsKey(entry.getKey());
        if (memoryMode == REUSABLE_DATA && !pointsContainsKey) {
          reusablePointsPool.returnObject(entry.getValue());
        }
        return !pointsContainsKey;
      });

      points.forEach((k, v) ->
          lastPoints.compute(k, (k2, v2) -> {
            T newValue = null;
            switch (memoryMode) {
              case REUSABLE_DATA:
                if (v2 == null) {
                  T newResuablePoint = reusablePointsPool.borrowObject();
                  aggregator.copyPoint(v, newResuablePoint);
                  newValue = newResuablePoint;
                } else {
                  aggregator.diffInPlace(v2, v);
                  newValue = v2;
                }
                break;
              case IMMUTABLE_DATA:
                newValue = (v2 == null) ? v : aggregator.diff(v2, v);
                break;
            }
            return newValue;
          }));

      result = lastPoints;
      this.lastPoints = points;
    } else {
      result = points;
    }

    Collection<T> resultCollection;
    if (memoryMode == REUSABLE_DATA) {
      if (previousCollectResult != null) {
        previousCollectResult.clear();
        this.points = previousCollectResult;
      } else {
        this.points = new PooledHashMap<>();
      }
      this.previousCollectResult = (PooledHashMap<Attributes, T>) result;
      if (previousReturnedValues == null) {
        previousReturnedValues = new ArrayList<>(previousCollectResult.size());
      }
      previousCollectResult.fillWithValues(previousReturnedValues);
      resultCollection = previousReturnedValues;
    } else {
      this.points = new HashMap<>();
      resultCollection = result.values();
    }

    return aggregator.toMetricData(
        resource,
        instrumentationScopeInfo,
        metricDescriptor,
        resultCollection,
        aggregationTemporality);
  }

  @Override
  public boolean isEmpty() {
    return aggregator == Aggregator.drop();
  }
}
