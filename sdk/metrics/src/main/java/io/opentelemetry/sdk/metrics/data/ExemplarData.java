/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanContext;
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
   * included.
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
}
