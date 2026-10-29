/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.metrics;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongUpDownCounter;

/** Extended {@link LongUpDownCounter} with experimental APIs. */
public interface ExtendedLongUpDownCounter extends LongUpDownCounter {

  /**
   * Binds this counter to the given {@code attributes}, returning a {@link BoundLongUpDownCounter}
   * that records to the corresponding timeseries directly.
   *
   * <p>Binding resolves the underlying timeseries once, eliminating the per-recording attribute
   * processing and map lookup performed by {@link #add(long, Attributes)}. Prefer this when the set
   * of attribute combinations is known ahead of time and the same series is recorded to repeatedly.
   */
  BoundLongUpDownCounter bind(Attributes attributes);

  // keep this class even if it is empty, since experimental methods may be added in the future.
}
