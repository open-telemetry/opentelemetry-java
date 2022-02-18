/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

import javax.annotation.concurrent.ThreadSafe;

/**
 * A registry for creating named {@link Tracer}s. The name <i>Provider</i> is for consistency with *
 * other languages and it is <b>NOT</b> loaded using reflection.
 *
 * @see Tracer
 */
@ThreadSafe
public interface TracerProvider {

  /**
   * Returns a no-op {@link TracerProvider} which only creates no-op {@link Span}s which do not
   * record nor are emitted.
   */
  static TracerProvider noop() {
    return DefaultTracerProvider.getInstance();
  }

  /**
   * Gets or creates a named tracer instance.
   *
   * @param instrumentationScopeName A name uniquely identifying the instrumentation scope, such as
   *     the instrumentation library, package, or fully qualified class name. Must not be null.
   * @return a tracer instance.
   */
  Tracer get(String instrumentationScopeName);

  /**
   * Gets or creates a named and versioned tracer instance.
   *
   * @param instrumentationScopeName A name uniquely identifying the instrumentation scope, such as
   *     the instrumentation library, package, or fully qualified class name. Must not be null.
   * @param instrumentationScopeVersion The version of the instrumentation scope (e.g., "1.0.0").
   * @return a tracer instance.
   */
  Tracer get(String instrumentationScopeName, String instrumentationScopeVersion);

  /**
   * Creates a TracerBuilder for a named {@link Tracer} instance.
   *
   * @param instrumentationScopeName A name uniquely identifying the instrumentation scope, such as
   *     the instrumentation library, package, or fully qualified class name. Must not be null.
   * @return a TracerBuilder instance.
   * @since 1.4.0
   */
  default TracerBuilder tracerBuilder(String instrumentationScopeName) {
    return DefaultTracerBuilder.getInstance();
  }
}
