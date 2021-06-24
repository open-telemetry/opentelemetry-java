/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.Attributes;
import javax.annotation.concurrent.Immutable;

@Immutable
@AutoValue
public abstract class LongExemplar implements Exemplar {

  public static LongExemplar create(
      Attributes filteredAttributes,
      long recordTimeNanos,
      String spanId,
      String traceId,
      long value) {
    return new AutoValue_LongExemplar(filteredAttributes, recordTimeNanos, spanId, traceId, value);
  }

  LongExemplar() {}

  /** Numerical value of the measurement that was recorded. */
  public abstract long getValue();
}
