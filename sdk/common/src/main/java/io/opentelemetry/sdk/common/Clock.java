/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common;

/** Interface for getting the current time. */
public interface Clock {
  /**
   * Obtains the current epoch timestamp in nanos from this clock.
   *
   * @return the current epoch timestamp in nanos.
   */
  long now();

  /**
   * Returns a time measurement with nanosecond precision that can only be used to calculate elapsed
   * time.
   *
   * @return a time measurement with nanosecond precision that can only be used to calculate elapsed
   *     time.
   */
  long nanoTime();
}
