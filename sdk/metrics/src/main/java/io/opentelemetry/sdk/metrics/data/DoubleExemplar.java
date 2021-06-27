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
