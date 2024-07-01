/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.metrics;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongHistogram;
import io.opentelemetry.context.Context;

/** Extended {@link LongHistogram} with experimental APIs. */
public interface ExtendedLongHistogram extends LongHistogram {

  /**
   * Returns {@code true} if the histogram is enabled.
   *
   * <p>This allows callers to avoid unnecessary compute when nothing is consuming the data. Because
   * the response is subject to change over the application, callers should call this before each
   * call to {@link #record(long)}, {@link #record(long, Attributes)}, or {@link #record(long,
   * Attributes, Context)}.
   */
  default boolean isEnabled() {
    return true;
  }
}
