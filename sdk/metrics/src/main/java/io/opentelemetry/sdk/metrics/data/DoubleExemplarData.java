/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.Attributes;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/** An {@link ExemplarData} with {@code double} measurments. */
@Immutable
@AutoValue
public abstract class DoubleExemplarData implements ExemplarData {

  /**
   * Construct a new exemplar.
   *
   * @param filteredAttributes The set of {@link Attributes} not already associated with the {@link
   *     PointData}.
   * @param recordTimeNanos The time when the sample qas recorded in nanoseconds.
   * @param spanId (optional) The associated SpanId.
   * @param traceId (optional) The associated TraceId.
   * @param value The value recorded.
   */
  public static DoubleExemplarData create(
      Attributes filteredAttributes,
      long recordTimeNanos,
      @Nullable String spanId,
      @Nullable String traceId,
      double value) {
    return new AutoValue_DoubleExemplarData(
        filteredAttributes, recordTimeNanos, spanId, traceId, value);
  }

  DoubleExemplarData() {}

  /** Numerical value of the measurement that was recorded. */
  public abstract double getValue();

  @Override
  public final double getValueAsDouble() {
    return getValue();
  }
}
