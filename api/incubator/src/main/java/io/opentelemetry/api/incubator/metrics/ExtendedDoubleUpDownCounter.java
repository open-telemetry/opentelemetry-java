/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.metrics;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleUpDownCounter;

/** Extended {@link DoubleUpDownCounter} with experimental APIs. */
public interface ExtendedDoubleUpDownCounter extends DoubleUpDownCounter {

  /**
   * Binds this counter to the given {@code attributes}, returning a {@link
   * BoundDoubleUpDownCounter} that records to the corresponding timeseries directly.
   *
   * <p>Binding resolves the underlying timeseries once, eliminating the per-recording attribute
   * processing and map lookup performed by {@link #add(double, Attributes)}. Prefer this when the
   * set of attribute combinations is known ahead of time and the same series is recorded to
   * repeatedly.
   */
  BoundDoubleUpDownCounter bind(Attributes attributes);

  // keep this class even if it is empty, since experimental methods may be added in the future.
}
