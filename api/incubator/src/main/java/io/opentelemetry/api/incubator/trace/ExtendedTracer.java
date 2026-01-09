/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.trace;

import io.opentelemetry.api.trace.Tracer;

/** Extended {@link Tracer} with experimental APIs. */
public interface ExtendedTracer extends Tracer {

  /**
   * Returns {@code true} if the tracer is enabled.
   *
   * <p>This allows callers to avoid unnecessary compute when nothing is consuming the data. Because
   * the response is subject to change over the application, callers should call this before each
   * call to {@link #spanBuilder(String)}.
   */
  default boolean isEnabled() {
    return true;
  }

  @Override
  ExtendedSpanBuilder spanBuilder(String spanName);
}
