/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import javax.annotation.concurrent.ThreadSafe;

/** A {@code Bound Instrument} for a {@link LongUpDownCounter}. */
@ThreadSafe
public interface BoundLongUpDownCounter extends BoundSynchronousInstrument {

  /**
   * Adds the given {@code increment} to the current value.
   *
   * <p>The value added is associated with the current {@code Context}.
   *
   * @param increment the value to add.
   */
  void add(long increment);

  @Override
  void unbind();
}
