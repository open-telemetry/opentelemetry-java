/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableLongExemplarData;
import javax.annotation.concurrent.Immutable;

/**
 * Exemplar data for {@code long} measurements.
 *
 * @since 1.14.0
 */
@Immutable
public interface LongExemplarData extends ExemplarData {

  /**
   * Create a record.
   *
   * @since 1.50.0
   */
  static LongExemplarData create(
      Attributes filteredAttributes, long recordTimeNanos, SpanContext spanContext, long value) {
    return ImmutableLongExemplarData.create(
        filteredAttributes, recordTimeNanos, spanContext, value);
  }

  /** Numerical value of the measurement that was recorded. */
  long getValue();
}
