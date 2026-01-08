/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.metrics.internal.exemplar.AlwaysOffExemplarFilter;
import io.opentelemetry.sdk.metrics.internal.exemplar.AlwaysOnExemplarFilter;
import io.opentelemetry.sdk.metrics.internal.exemplar.DoubleExemplarReservoir;
import io.opentelemetry.sdk.metrics.internal.exemplar.LongExemplarReservoir;
import io.opentelemetry.sdk.metrics.internal.exemplar.TraceBasedExemplarFilter;

/**
 * Exemplar filters are used to pre-filter measurements before attempting to store them in a
 * reservoir ({@link DoubleExemplarReservoir}, {@link LongExemplarReservoir}.
 *
 * @see SdkMeterProviderBuilder#setExemplarFilter(ExemplarFilter)
 * @since 1.56.0
 */
// TODO(jack-berg): Have methods when custom filters are supported.
@SuppressWarnings("InterfaceWithOnlyStatics")
public interface ExemplarFilter {
  /**
   * A filter that only accepts measurements where there is a {@code Span} in {@link Context} that
   * is being sampled.
   */
  static ExemplarFilter traceBased() {
    return TraceBasedExemplarFilter.getInstance();
  }

  /** A filter which makes all measurements eligible for being an exemplar. */
  static ExemplarFilter alwaysOn() {
    return AlwaysOnExemplarFilter.getInstance();
  }

  /** A filter which makes no measurements eligible for being an exemplar. */
  static ExemplarFilter alwaysOff() {
    return AlwaysOffExemplarFilter.getInstance();
  }
}
