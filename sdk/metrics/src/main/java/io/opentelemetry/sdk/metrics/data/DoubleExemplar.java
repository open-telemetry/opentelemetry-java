/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.Attributes;
import javax.annotation.concurrent.Immutable;

/** An {@link Exemplar} with {@code double} measurments. */
@Immutable
@AutoValue
public abstract class DoubleExemplar implements Exemplar {

  /**
   * Construct a new exemplar.
   *
   * @param filteredAttributes The set of {@link Attributes} not already associated with the {@link
   *     PointData}.
   * @param recordTimeNanos The time when the sample qas recorded in nanoseconds.
   * @param spanId (optional) The associated {@code SpanId}
   * @param traceId (optional) The associated {@code TraceId}
   * @param value The value recorded.
   */
  public static DoubleExemplar create(
      Attributes filteredAttributes,
      long recordTimeNanos,
      String spanId,
      String traceId,
      double value) {
    return new AutoValue_DoubleExemplar(
        filteredAttributes, recordTimeNanos, spanId, traceId, value);
  }

  DoubleExemplar() {}

  /** Numerical value of the measurement that was recorded. */
  public abstract double getValue();
}
