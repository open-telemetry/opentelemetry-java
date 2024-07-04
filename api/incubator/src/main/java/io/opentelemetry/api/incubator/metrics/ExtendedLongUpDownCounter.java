/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.metrics;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongUpDownCounter;
import io.opentelemetry.context.Context;

/** Extended {@link LongUpDownCounter} with experimental APIs. */
public interface ExtendedLongUpDownCounter extends LongUpDownCounter {

  /**
   * Returns {@code true} if the up down counter is enabled.
   *
   * <p>This allows callers to avoid unnecessary compute when nothing is consuming the data. Because
   * the response is subject to change over the application, callers should call this before each
   * call to {@link #add(long)}, {@link #add(long, Attributes)}, or {@link #add(long, Attributes,
   * Context)}.
   */
  default boolean isEnabled() {
    return true;
  }
}
