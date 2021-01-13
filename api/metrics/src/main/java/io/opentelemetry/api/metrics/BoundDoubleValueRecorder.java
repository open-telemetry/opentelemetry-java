/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import javax.annotation.concurrent.ThreadSafe;

/** A {@code Bound Instrument} for a {@link DoubleValueRecorder}. */
@ThreadSafe
public interface BoundDoubleValueRecorder extends BoundSynchronousInstrument {
  /**
   * Records the given measurement, associated with the current {@code Context}.
   *
   * @param value the measurement to record.
   * @throws IllegalArgumentException if value is negative.
   */
  void record(double value);

  @Override
  void unbind();
}
