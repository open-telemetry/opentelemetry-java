/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.trace;

import io.opentelemetry.api.trace.Tracer;

/**
 * Utility class to simplify tracing.
 *
 * <p>The <a
 * href="https://github.com/opentelemetry/opentelemetry-java/blob/main/api/incubator">README</a>
 * explains the use cases in more detail.
 */
public final class ExtendedTracer implements Tracer {

  private final Tracer delegate;

  private ExtendedTracer(Tracer delegate) {
    this.delegate = delegate;
  }

  /**
   * Creates a new instance of {@link ExtendedTracer}.
   *
   * @param delegate the {@link Tracer} to use
   */
  public static ExtendedTracer create(Tracer delegate) {
    return new ExtendedTracer(delegate);
  }

  /**
   * Creates a new {@link ExtendedSpanBuilder} with the given span name.
   *
   * @param spanName the name of the span
   * @return the {@link ExtendedSpanBuilder}
   */
  @Override
  public ExtendedSpanBuilder spanBuilder(String spanName) {
    return new ExtendedSpanBuilder(delegate.spanBuilder(spanName));
  }
}
