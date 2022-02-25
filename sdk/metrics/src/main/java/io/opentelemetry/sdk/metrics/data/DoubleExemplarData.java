/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanContext;
import javax.annotation.concurrent.Immutable;

/** An {@link ExemplarData} with {@code double} measurements. */
@Immutable
@AutoValue
public abstract class DoubleExemplarData implements ExemplarData {

  /**
   * Construct a new exemplar.
   *
   * @param filteredAttributes The set of {@link Attributes} not already associated with the {@link
   *     PointData}.
   * @param recordTimeNanos The time when the sample qas recorded in nanoseconds.
   * @param spanContext The associated span context.
   * @param value The value recorded.
   */
  public static DoubleExemplarData create(
      Attributes filteredAttributes, long recordTimeNanos, SpanContext spanContext, double value) {
    return new AutoValue_DoubleExemplarData(
        filteredAttributes, recordTimeNanos, spanContext, value);
  }

  DoubleExemplarData() {}

  /** Numerical value of the measurement that was recorded. */
  public abstract double getValue();

  @Override
  @Deprecated
  public final double getValueAsDouble() {
    return getValue();
  }
}
