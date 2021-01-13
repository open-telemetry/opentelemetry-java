/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import javax.annotation.concurrent.ThreadSafe;

/** A {@code Bound Instrument} for a {@link LongValueRecorder}. */
@ThreadSafe
public interface BoundLongValueRecorder extends BoundSynchronousInstrument {
  /**
   * Records the given measurement, associated with the current {@code Context}.
   *
   * @param value the measurement to record.
   * @throws IllegalArgumentException if value is negative.
   */
  void record(long value);

  @Override
  void unbind();
}
