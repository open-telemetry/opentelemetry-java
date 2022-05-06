/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.exemplar;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.metrics.data.DoubleExemplarData;
import io.opentelemetry.sdk.metrics.data.ExemplarData;
import io.opentelemetry.sdk.metrics.data.LongExemplarData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableDoubleExemplarData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableLongExemplarData;
import java.util.Set;
import javax.annotation.Nullable;

/**
 * A Reservoir cell pre-allocated memories for Exemplar data.
 *
 * <p>We only allocate new objects during collection. This class should NOT cause allocations during
 * sampling or within the synchronous metric hot-path.
 *
 * <p>Allocations are acceptable in the {@link #getAndResetDouble(Attributes)} and {@link
 * #getAndResetLong(Attributes)} collection methods.
 */
class ReservoirCell {
  private final Clock clock;
  @Nullable private Attributes attributes;
  private SpanContext spanContext = SpanContext.getInvalid();
  private long recordTime;

  // Cell stores either long or double values, but must not store both
  private long longValue;
  private double doubleValue;

  ReservoirCell(Clock clock) {
    this.clock = clock;
  }

  /**
   * Record the long measurement to the cell.
   *
   * <p>Must be used in tandem with {@link #getAndResetLong(Attributes)}. {@link
   * #recordDoubleMeasurement(double, Attributes, Context)} and {@link
   * #getAndResetDouble(Attributes)} must not be used when a cell is recording longs.
   */
  synchronized void recordLongMeasurement(long value, Attributes attributes, Context context) {
    this.longValue = value;
    offerMeasurement(attributes, context);
  }

  /**
   * Record the long measurement to the cell.
   *
   * <p>Must be used in tandem with {@link #getAndResetDouble(Attributes)}. {@link
   * #recordLongMeasurement(long, Attributes, Context)} and {@link #getAndResetLong(Attributes)}
   * must not be used when a cell is recording longs.
   */
  synchronized void recordDoubleMeasurement(double value, Attributes attributes, Context context) {
    this.doubleValue = value;
    offerMeasurement(attributes, context);
  }

  private void offerMeasurement(Attributes attributes, Context context) {
    this.attributes = attributes;
    // Note: It may make sense in the future to attempt to pull this from an active span.
    this.recordTime = clock.now();
    Span current = Span.fromContext(context);
    if (current.getSpanContext().isValid()) {
      this.spanContext = current.getSpanContext();
    }
  }

  /**
   * Retrieve the cell's {@link ExemplarData}.
   *
   * <p>Must be used in tandem with {@link #recordLongMeasurement(long, Attributes, Context)}.
   */
  @Nullable
  synchronized LongExemplarData getAndResetLong(Attributes pointAttributes) {
    Attributes attributes = this.attributes;
    if (attributes == null) {
      return null;
    }
    LongExemplarData result =
        ImmutableLongExemplarData.create(
            filtered(attributes, pointAttributes), recordTime, spanContext, longValue);
    reset();
    return result;
  }

  /**
   * Retrieve the cell's {@link ExemplarData}.
   *
   * <p>Must be used in tandem with {@link #recordDoubleMeasurement(double, Attributes, Context)}.
   */
  @Nullable
  synchronized DoubleExemplarData getAndResetDouble(Attributes pointAttributes) {
    Attributes attributes = this.attributes;
    if (attributes == null) {
      return null;
    }
    DoubleExemplarData result =
        ImmutableDoubleExemplarData.create(
            filtered(attributes, pointAttributes), recordTime, spanContext, doubleValue);
    reset();
    return result;
  }

  synchronized void reset() {
    this.attributes = null;
    this.longValue = 0;
    this.doubleValue = 0;
    this.spanContext = SpanContext.getInvalid();
    this.recordTime = 0;
  }

  /** Returns filtered attributes for exemplars. */
  private static Attributes filtered(Attributes original, Attributes metricPoint) {
    if (metricPoint.isEmpty()) {
      return original;
    }
    Set<AttributeKey<?>> metricPointKeys = metricPoint.asMap().keySet();
    return original.toBuilder().removeIf(metricPointKeys::contains).build();
  }
}
