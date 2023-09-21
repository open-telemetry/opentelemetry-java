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
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

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

  /**
   * This field is set to 1 less than the actual intended cardinality limit, allowing the last slot
   * to be filled by the {@link MetricStorage#CARDINALITY_OVERFLOW} series.
   */
  private final int maxCardinality;

  private Map<Attributes, T> points;

  // Only populated if aggregationTemporality == DELTA
  private Map<Attributes, T> lastPoints;

  // Only populated if memoryMode == REUSABLE_DATA
  private final ObjectPool<T> reusablePointsPool;

  // Only populated if memoryMode == REUSABLE_DATA
  private final ArrayList<T> reusableResultList = new ArrayList<>();

  private final MemoryMode memoryMode;

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

    measurement = measurement.withAttributes(processedAttributes).withStartEpochNanos(start);

    recordPoint(processedAttributes, measurement);
  }

  private void recordPoint(Attributes attributes, Measurement measurement) {
    if (points.size() >= maxCardinality) {
      throttlingLogger.log(
          Level.WARNING,
          "Instrument "
              + metricDescriptor.getSourceInstrument().getName()
              + " has exceeded the maximum allowed cardinality ("
              + maxCardinality
              + ").");
      attributes = MetricStorage.CARDINALITY_OVERFLOW;
      measurement = measurement.withAttributes(attributes);
    } else if (points.containsKey(
        attributes)) { // Check there is not already a recording for the attributes
      throttlingLogger.log(
          Level.WARNING,
          "Instrument "
              + metricDescriptor.getSourceInstrument().getName()
              + " has recorded multiple values for the same attributes: "
              + attributes);
      return;
    }

    T dataPoint;
    if (memoryMode == REUSABLE_DATA) {
      dataPoint = reusablePointsPool.borrowObject();
      aggregator.toPoint(measurement, dataPoint);
    } else {
      dataPoint = aggregator.toPoint(measurement);
    }

    points.put(attributes, dataPoint);
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
      reusableResultList.forEach(reusablePointsPool::returnObject);
      reusableResultList.clear();
    }

    Collection<T> result;
    if (aggregationTemporality == AggregationTemporality.DELTA) {
      Map<Attributes, T> points = this.points;
      Map<Attributes, T> lastPoints = this.lastPoints;

      Collection<T> deltaPoints;
      if (memoryMode == REUSABLE_DATA) {
        deltaPoints = reusableResultList;
      } else {
        deltaPoints = new ArrayList<>();
      }

      points.forEach(
          (k, v) -> {
            T lastPoint = lastPoints.get(k);

            T deltaPoint;
            if (lastPoint == null) {
              if (memoryMode == REUSABLE_DATA) {
                deltaPoint = reusablePointsPool.borrowObject();
                aggregator.copyPoint(v, deltaPoint);
              } else {
                deltaPoint = v;
              }
            } else {
              if (memoryMode == REUSABLE_DATA) {
                aggregator.diffInPlace(lastPoint, v);
                deltaPoint = lastPoint;

                // Remaining last points are returned to reusablePointsPool, but
                // this reusable point is still used, so don't return it to pool yet
                lastPoints.remove(k);
              } else {
                deltaPoint = aggregator.diff(lastPoint, v);
              }
            }

            deltaPoints.add(deltaPoint);
          });

      if (memoryMode == REUSABLE_DATA) {
        lastPoints.forEach((k, v) -> reusablePointsPool.returnObject(v));
        lastPoints.clear();
        this.points = lastPoints;
      } else {
        this.points = new HashMap<>();
      }

      this.lastPoints = points;
      result = deltaPoints;
    } else /* CUMULATIVE */ {
      if (memoryMode == REUSABLE_DATA) {
        points.forEach((k, v) -> reusableResultList.add(v));
        points.clear();
        result = reusableResultList;
      } else {
        result = points.values();
        points = new HashMap<>();
      }
    }

    return aggregator.toMetricData(
        resource, instrumentationScopeInfo, metricDescriptor, result, aggregationTemporality);
  }

  @Override
  public boolean isEmpty() {
    return aggregator == Aggregator.drop();
  }
}
