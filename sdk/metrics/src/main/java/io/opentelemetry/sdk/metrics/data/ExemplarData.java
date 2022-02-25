/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanContext;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * A sample input measurement.
 *
 * <p>Exemplars also hold information about the environment when the measurement was recorded, for
 * example the span and trace ID of the active span when the exemplar was recorded.
 */
@Immutable
public interface ExemplarData {
  /**
   * The set of key/value pairs that were filtered out by the aggregator, but recorded alongside the
   * original measurement. Only key/value pairs that were filtered out by the aggregator should be
   * included
   */
  Attributes getFilteredAttributes();

  /** Returns the timestamp in nanos when measurement was collected. */
  long getEpochNanos();

  /**
   * Returns the {@link SpanContext} associated with this exemplar. If the exemplar was not recorded
   * inside a sampled trace, the {@link SpanContext} will be {@linkplain SpanContext#getInvalid()
   * invalid}.
   */
  SpanContext getSpanContext();

  /**
   * (Optional) Span ID of the exemplar trace.
   *
   * <p>Span ID may be {@code null} if the measurement is not recorded inside a trace or the trace
   * was not sampled.
   *
   * @deprecated Use {@link ExemplarData#getSpanContext()}.
   */
  @Nullable
  @Deprecated
  default String getSpanId() {
    SpanContext spanContext = getSpanContext();
    return spanContext != null ? spanContext.getSpanId() : null;
  }
  /**
   * (Optional) Trace ID of the exemplar trace.
   *
   * <p>Trace ID may be {@code null} if the measurement is not recorded inside a trace or if the
   * trace is not sampled.
   *
   * @deprecated Use {@link ExemplarData#getSpanContext()}.
   */
  @Nullable
  @Deprecated
  default String getTraceId() {
    SpanContext spanContext = getSpanContext();
    return spanContext != null ? spanContext.getTraceId() : null;
  }

  /**
   * Coerces this exemplar to a double value.
   *
   * <p>Note: This could create a loss of precision from {@code long} measurements.
   *
   * @deprecated Cast to {@link LongExemplarData} or {@link DoubleExemplarData} and call {@code
   *     getValue()}.
   */
  @Deprecated
  double getValueAsDouble();
}
