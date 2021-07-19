/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import io.opentelemetry.api.common.Attributes;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * A sample input measurement.
 *
 * <p>Exemplars also hold information about the environment when the measurement was recorded, for
 * example the span and trace ID of the active span when the exemplar was recorded.
 */
@Immutable
public interface Exemplar {
  /**
   * The set of key/value pairs that were filtered out by the aggregator, but recorded alongside the
   * original measurement. Only key/value pairs that were filtered out by the aggregator should be
   * included
   */
  Attributes getFilteredAttributes();

  /** Returns the timestamp in nanos when measurement was collected. */
  long getEpochNanos();

  /**
   * (Optional) Span ID of the exemplar trace.
   *
   * <p>Span ID may be {@code null} if the measurement is not recorded inside a trace or the trace
   * was not sampled.
   */
  @Nullable
  String getSpanId();
  /**
   * (Optional) Trace ID of the exemplar trace.
   *
   * <p>Trace ID may be {@code null} if the measurement is not recorded inside a trace or if the
   * trace is not sampled.
   */
  @Nullable
  String getTraceId();

  /**
   * Coerces this exemplar to a double value.
   *
   * <p>Note: This could createa a loss of precision from {@code long} measurements.
   */
  double getValueAsDouble();

  /**
   * Utility method that will remove attribtues from an exemplar and return a new one with the
   * filtered set.
   *
   * @param pointAttributes The attributes included in a metric point.
   * @return The resulting exemplar with filtered attribtues being further filtered.
   */
  Exemplar filterAttributes(Attributes pointAttributes);
}
