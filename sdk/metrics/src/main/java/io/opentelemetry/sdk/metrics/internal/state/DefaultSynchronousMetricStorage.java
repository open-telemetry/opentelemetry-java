/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import static io.opentelemetry.sdk.metrics.data.AggregationTemporality.CUMULATIVE;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.internal.ThrottlingLogger;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.PointData;
import io.opentelemetry.sdk.metrics.internal.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.internal.aggregator.AggregatorHandle;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.metrics.internal.export.RegisteredReader;
import io.opentelemetry.sdk.metrics.internal.view.AttributesProcessor;
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
public abstract class DefaultSynchronousMetricStorage<T extends PointData>
    implements SynchronousMetricStorage {

  private static final Logger internalLogger =
      Logger.getLogger(DefaultSynchronousMetricStorage.class.getName());

  final ThrottlingLogger logger = new ThrottlingLogger(internalLogger);
  final AttributesProcessor attributesProcessor;
  protected final Clock clock;
  protected final MetricDescriptor metricDescriptor;
  protected final Aggregator<T> aggregator;

  /**
   * This field is set to 1 less than the actual intended cardinality limit, allowing the last slot
   * to be filled by the {@link MetricStorage#CARDINALITY_OVERFLOW} series.
   */
  protected final int maxCardinality;

  protected volatile boolean enabled;

  DefaultSynchronousMetricStorage(
      MetricDescriptor metricDescriptor,
      Aggregator<T> aggregator,
      AttributesProcessor attributesProcessor,
      Clock clock,
      int maxCardinality,
      boolean enabled) {
    this.metricDescriptor = metricDescriptor;
    this.aggregator = aggregator;
    this.attributesProcessor = attributesProcessor;
    this.clock = clock;
    this.maxCardinality = maxCardinality - 1;
    this.enabled = enabled;
  }

  static <T extends PointData> DefaultSynchronousMetricStorage<T> create(
      RegisteredReader reader,
      MetricDescriptor descriptor,
      Aggregator<T> aggregator,
      AttributesProcessor processor,
      int maxCardinality,
      Clock clock,
      boolean enabled) {
    AggregationTemporality aggregationTemporality =
        reader.getReader().getAggregationTemporality(descriptor.getSourceInstrument().getType());
    return aggregationTemporality == CUMULATIVE
        ? new CumulativeSynchronousMetricStorage<>(
            descriptor,
            aggregator,
            processor,
            clock,
            maxCardinality,
            enabled,
            reader.getReader().getMemoryMode())
        : new DeltaSynchronousMetricStorage<>(
            reader, descriptor, aggregator, processor, clock, maxCardinality, enabled);
  }

  @Override
  public void recordLong(long value, Attributes attributes, Context context) {
    if (!enabled) {
      return;
    }
    doRecordLong(value, attributes, context);
  }

  @Override
  public void recordDouble(double value, Attributes attributes, Context context) {
    if (!enabled) {
      return;
    }
    if (Double.isNaN(value)) {
      logger.log(
          Level.FINE,
          "Instrument "
              + metricDescriptor.getSourceInstrument().getName()
              + " has recorded measurement Not-a-Number (NaN) value with attributes "
              + attributes
              + ". Dropping measurement.");
      return;
    }
    doRecordDouble(value, attributes, context);
  }

  abstract void doRecordLong(long value, Attributes attributes, Context context);

  abstract void doRecordDouble(double value, Attributes attributes, Context context);

  @Override
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  protected AggregatorHandle<T> getAggregatorHandle(
      ConcurrentHashMap<Attributes, AggregatorHandle<T>> aggregatorHandles,
      Attributes attributes,
      Context context) {
    Objects.requireNonNull(attributes, "attributes");
    attributes = attributesProcessor.process(attributes, context);
    AggregatorHandle<T> handle = aggregatorHandles.get(attributes);
    if (handle != null) {
      return handle;
    }
    if (aggregatorHandles.size() >= maxCardinality) {
      logger.log(
          Level.WARNING,
          "Instrument "
              + metricDescriptor.getSourceInstrument().getName()
              + " has exceeded the maximum allowed cardinality ("
              + maxCardinality
              + ").");
      // Return handle for overflow series, first checking if a handle already exists for it
      attributes = MetricStorage.CARDINALITY_OVERFLOW;
      handle = aggregatorHandles.get(attributes);
      if (handle != null) {
        return handle;
      }
    }
    // Get handle from pool if available, else create a new one.
    // Note: pooled handles (used only for delta temporality) retain their original
    // creationEpochNanos, but delta storage does not use the handle's creation time for the
    // start epoch — it uses the reader's last collect time directly in collect(). So the stale
    // creation time on a recycled handle does not affect correctness.
    AggregatorHandle<T> newHandle = maybeGetPooledAggregatorHandle();
    if (newHandle == null) {
      newHandle = aggregator.createHandle(clock.now());
    }
    handle = aggregatorHandles.putIfAbsent(attributes, newHandle);
    return handle != null ? handle : newHandle;
  }

  @Nullable
  abstract AggregatorHandle<T> maybeGetPooledAggregatorHandle();

  @Override
  public MetricDescriptor getMetricDescriptor() {
    return metricDescriptor;
  }
}
