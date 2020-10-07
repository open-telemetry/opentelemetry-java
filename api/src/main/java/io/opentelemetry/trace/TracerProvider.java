/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.trace;

import io.opentelemetry.trace.spi.TracerProviderFactory;
import javax.annotation.concurrent.ThreadSafe;

/**
 * A registry for creating named {@link Tracer}s. Although the class is provided at runtime via
 * {@link TracerProviderFactory}, the name <i>Provider</i> is for consistency with other languages.
 *
 * @see io.opentelemetry.OpenTelemetry
 * @see io.opentelemetry.trace.Tracer
 * @since 0.1.0
 */
@ThreadSafe
public interface TracerProvider {

  /**
   * Gets or creates a named tracer instance.
   *
   * @param instrumentationName The name of the instrumentation library, not the name of the
   *     instrument*ed* library (e.g., "io.opentelemetry.contrib.mongodb"). Must not be null.
   * @return a tracer instance.
   * @since 0.1.0
   */
  Tracer get(String instrumentationName);

  /**
   * Gets or creates a named and versioned tracer instance.
   *
   * @param instrumentationName The name of the instrumentation library, not the name of the
   *     instrument*ed* library (e.g., "io.opentelemetry.contrib.mongodb"). Must not be null.
   * @param instrumentationVersion The version of the instrumentation library (e.g.,
   *     "semver:1.0.0").
   * @return a tracer instance.
   * @since 0.1.0
   */
  Tracer get(String instrumentationName, String instrumentationVersion);
}
