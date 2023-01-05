/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.exemplar;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;

/**
 * Exemplar filters are used to pre-filter measurements before attempting to store them in a
 * reservoir.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public interface ExemplarFilter {
  /** Returns whether or not a reservoir should attempt to filter a measurement. */
  boolean shouldSampleMeasurement(long value, Attributes attributes, Context context);

  /** Returns whether or not a reservoir should attempt to filter a measurement. */
  boolean shouldSampleMeasurement(double value, Attributes attributes, Context context);

  /**
   * A filter that only accepts measurements where there is a {@code Span} in {@link Context} that
   * is being sampled.
   */
  static ExemplarFilter traceBased() {
    return TraceBasedExemplarFilter.INSTANCE;
  }

  /** A filter which makes all measurements eligible for being an exemplar. */
  static ExemplarFilter alwaysOn() {
    return AlwaysOnFilter.INSTANCE;
  }

  /** A filter which makes no measurements eligible for being an exemplar. */
  static ExemplarFilter alwaysOff() {
    return AlwaysOffFilter.INSTANCE;
  }
}
