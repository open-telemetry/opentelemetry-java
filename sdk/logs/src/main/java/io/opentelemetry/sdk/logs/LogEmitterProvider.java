/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import javax.annotation.concurrent.ThreadSafe;

/**
 * A registry for creating named {@link LogEmitter}s.
 *
 * <p>A LogEmitterProvider represents a configured (or noop) Log collection system that can be used
 * to instrument code.
 *
 * <p>The name <i>Provider</i> is for consistency with other languages and it is <b>NOT</b> loaded
 * using reflection.
 */
@ThreadSafe
public interface LogEmitterProvider {

  /**
   * Gets or creates a named and versioned log instance.
   *
   * @param instrumentationName The name of the instrumentation library, not the name of the
   *     instrument*ed* library.
   * @return a log emitter instance.
   */
  default LogEmitter get(String instrumentationName) {
    return logEmitterBuilder(instrumentationName).build();
  }

  /**
   * Creates a LogBuilder for a named log instance.
   *
   * @param instrumentationName The name of the instrumentation library, not the name of the
   *     instrument*ed* library.
   * @return a LogEmitterBuilder instance.
   */
  LogEmitterBuilder logEmitterBuilder(String instrumentationName);

  /**
   * Returns a no-op {@link LogEmitterProvider} which provides meters which do not record or emit.
   */
  static LogEmitterProvider noop() {
    return DefaultLogEmitterProvider.getInstance();
  }
}
