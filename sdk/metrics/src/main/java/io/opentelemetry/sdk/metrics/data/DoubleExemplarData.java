/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableDoubleExemplarData;
import javax.annotation.concurrent.Immutable;

/**
 * Exemplar data for {@code double} measurements.
 *
 * @since 1.14.0
 */
@Immutable
public interface DoubleExemplarData extends ExemplarData {

  /**
   * Create a record.
   *
   * @since 1.50.0
   */
  static DoubleExemplarData create(
      Attributes filteredAttributes, long recordTimeNanos, SpanContext spanContext, double value) {
    return ImmutableDoubleExemplarData.create(
        filteredAttributes, recordTimeNanos, spanContext, value);
  }

  /** Numerical value of the measurement that was recorded. */
  double getValue();
}
