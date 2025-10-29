/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.exemplar;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.metrics.ExemplarFilter;

/**
 * Exemplar filters are used to pre-filter measurements before attempting to store them in a
 * reservoir.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public interface ExemplarFilterInternal extends ExemplarFilter {

  /**
   * Utility for casting {@link ExemplarFilter} to {@link ExemplarFilterInternal}, throwing if
   * {@code filter} does not implement {@link ExemplarFilterInternal}.
   */
  static ExemplarFilterInternal asExemplarFilterInternal(ExemplarFilter filter) {
    if (!(filter instanceof ExemplarFilterInternal)) {
      throw new IllegalArgumentException(
          "Custom ExemplarFilter implementations are currently not supported. "
              + "Use one of the standard implementations returned by the static factories in the ExemplarFilter class.");
    }
    return (ExemplarFilterInternal) filter;
  }

  /** Returns whether or not a reservoir should attempt to filter a measurement. */
  boolean shouldSampleMeasurement(long value, Attributes attributes, Context context);

  /** Returns whether or not a reservoir should attempt to filter a measurement. */
  boolean shouldSampleMeasurement(double value, Attributes attributes, Context context);
}
