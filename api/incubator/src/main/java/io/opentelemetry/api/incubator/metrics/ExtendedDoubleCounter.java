/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.metrics;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleCounter;
import io.opentelemetry.context.Context;

/** Extended {@link DoubleCounter} with experimental APIs. */
public interface ExtendedDoubleCounter extends DoubleCounter {

  /**
   * Returns {@code true} if the counter is enabled.
   *
   * <p>This allows callers to avoid unnecessary compute when nothing is consuming the data. Because
   * the response is subject to change over the application, callers should call this before each
   * call to {@link #add(double)}, {@link #add(double, Attributes)}, or {@link #add(double,
   * Attributes, Context)}.
   */
  default boolean enabled() {
    return true;
  }
}
