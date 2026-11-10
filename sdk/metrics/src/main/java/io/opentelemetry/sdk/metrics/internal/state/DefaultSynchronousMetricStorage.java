/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import static io.opentelemetry.sdk.metrics.data.AggregationTemporality.CUMULATIVE;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.internal.ThrottlingLogger;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.PointData;
import io.opentelemetry.sdk.metrics.internal.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.metrics.internal.export.RegisteredReader;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
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
  final UnaryOperator<Attributes> attributesFilter;
  @Nullable private final Predicate<AttributeKey<?>> contextScopedAttributesFilter;
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
      UnaryOperator<Attributes> attributesFilter,
      Clock clock,
      int maxCardinality,
      boolean enabled,
      @Nullable Predicate<AttributeKey<?>> contextScopedAttributesFilter) {
    this.metricDescriptor = metricDescriptor;
    this.aggregator = aggregator;
    this.attributesFilter = attributesFilter;
    this.clock = clock;
    this.maxCardinality = maxCardinality - 1;
    this.enabled = enabled;
    this.contextScopedAttributesFilter = contextScopedAttributesFilter;
  }

  static <T extends PointData> DefaultSynchronousMetricStorage<T> create(
      RegisteredReader reader,
      MetricDescriptor descriptor,
      Aggregator<T> aggregator,
      UnaryOperator<Attributes> attributesFilter,
      int maxCardinality,
      Clock clock,
      boolean enabled) {
    return create(
        reader,
        descriptor,
        aggregator,
        attributesFilter,
        maxCardinality,
        clock,
        enabled,
        /* contextScopedAttributesFilter= */ null);
  }

  static <T extends PointData> DefaultSynchronousMetricStorage<T> create(
      RegisteredReader reader,
      MetricDescriptor descriptor,
      Aggregator<T> aggregator,
      UnaryOperator<Attributes> attributesFilter,
      int maxCardinality,
      Clock clock,
      boolean enabled,
      @Nullable Predicate<AttributeKey<?>> contextScopedAttributesFilter) {
    AggregationTemporality aggregationTemporality =
        reader.getReader().getAggregationTemporality(descriptor.getSourceInstrument().getType());
    return aggregationTemporality == CUMULATIVE
        ? new CumulativeSynchronousMetricStorage<>(
            descriptor,
            aggregator,
            attributesFilter,
            clock,
            maxCardinality,
            enabled,
            contextScopedAttributesFilter,
            reader.getReader().getMemoryMode())
        : new DeltaSynchronousMetricStorage<>(
            reader,
            descriptor,
            aggregator,
            attributesFilter,
            clock,
            maxCardinality,
            enabled,
            contextScopedAttributesFilter);
  }

  private static final boolean INCUBATOR_AVAILABLE;

  static {
    boolean incubatorAvailable = false;
    try {
      Class.forName("io.opentelemetry.api.incubator.common.ContextAttributes");
      incubatorAvailable = true;
    } catch (ClassNotFoundException e) {
      // Not available
    }
    INCUBATOR_AVAILABLE = incubatorAvailable;
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private Attributes getMergedContextAttributes(Attributes attributes, Context context) {
    if (contextScopedAttributesFilter == null || !INCUBATOR_AVAILABLE) {
      return attributes;
    }
    Attributes contextAttributes = StateIncubatingUtil.getContextAttributes(context);
    if (contextAttributes.isEmpty()) {
      return attributes;
    }
    AttributesBuilder[] builderRef = new AttributesBuilder[1];
    contextAttributes.forEach(
        (key, value) -> {
          if (contextScopedAttributesFilter.test(key)) {
            if (builderRef[0] == null) {
              builderRef[0] = Attributes.builder();
            }
            builderRef[0].put((AttributeKey) key, value);
          }
        });
    AttributesBuilder builder = builderRef[0];
    if (builder == null) {
      return attributes;
    }
    if (attributes.isEmpty()) {
      return builder.build();
    }
    return builder.putAll(attributes).build();
  }

  @Override
  public void recordLong(long value, Attributes attributes, Context context) {
    if (!enabled) {
      return;
    }
    doRecordLong(value, getMergedContextAttributes(attributes, context), context);
  }

  @Override
  public void recordDouble(double value, Attributes attributes, Context context) {
    if (!enabled) {
      return;
    }
    attributes = getMergedContextAttributes(attributes, context);
    if (!shouldRecordDouble(value, attributes)) {
      return;
    }
    doRecordDouble(value, attributes, context);
  }

  /**
   * Returns true if a double {@code value} should be recorded. Returns false (dropping the
   * measurement) when recording is disabled, or when {@code value} is NaN, logging in the latter
   * case. Shared by the unbound and bound record paths.
   */
  final boolean shouldRecordDouble(double value, Attributes attributes) {
    if (!enabled) {
      return false;
    }
    if (Double.isNaN(value)) {
      logger.log(
          Level.FINE,
          "Instrument "
              + metricDescriptor.getSourceInstrument().getName()
              + " has recorded measurement Not-a-Number (NaN) value with attributes "
              + attributes
              + ". Dropping measurement.");
      return false;
    }
    return true;
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

  @Override
  public MetricDescriptor getMetricDescriptor() {
    return metricDescriptor;
  }
}
