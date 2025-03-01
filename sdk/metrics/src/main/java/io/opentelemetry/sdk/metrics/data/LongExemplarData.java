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
   * Construct a new exemplar.
   *
   * @param filteredAttributes The set of {@link Attributes} not already associated with the {@link
   *     PointData}.
   * @param recordTimeNanos The time when the sample qas recorded in nanoseconds.
   * @param spanContext The associated span context.
   * @param value The value recorded.
   */
  static LongExemplarData create(
      Attributes filteredAttributes, long recordTimeNanos, SpanContext spanContext, long value) {
    return ImmutableLongExemplarData.create(
        filteredAttributes, recordTimeNanos, spanContext, value);
  }

  /** Numerical value of the measurement that was recorded. */
  long getValue();
}
