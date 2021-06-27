/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import io.opentelemetry.api.common.Attributes;
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
  long getRecordTimeNanos();

  /**
   * (Optional) Span ID of the exemplar trace.
   *
   * <p>span_id may be missing if the measurement is not recorded inside a trace or the trace was
   * not sampled.
   */
  String getSpanId();
  /**
   * (Optional) Trace ID of the exemplar trace.
   *
   * <p>trace_id may be missing if the measurement is not recorded inside a trace or if the trace is
   * not sampled.
   */
  String getTraceId();
}
