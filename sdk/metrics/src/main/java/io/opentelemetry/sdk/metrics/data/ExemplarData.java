/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanContext;
import javax.annotation.concurrent.Immutable;

/**
 * An example measurement.
 *
 * <p>Provides details about a measurement that are normally aggregated away, including the
 * measurement value, the measurement timestamp, and additional attributes.
 */
@Immutable
public interface ExemplarData {
  /**
   * Returns the attributes that were recorded alongside the original measurement but filtered out
   * by the aggregator.
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
