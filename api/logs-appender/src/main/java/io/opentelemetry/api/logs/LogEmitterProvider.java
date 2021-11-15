/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.logs;

import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public interface LogEmitterProvider {

  /**
   * Gets or creates a {@link LogEmitter} instance.
   *
   * @param instrumentationName the name of the instrumentation library
   * @return a lot emitter instance
   */
  default LogEmitter get(String instrumentationName) {
    return logEmitterBuilder(instrumentationName).build();
  }

  /**
   * Creates a {@link LogEmitterBuilder} instance.
   *
   * @param instrumentationName the name of the instrumentation library
   * @return a log emitter builder instance
   */
  LogEmitterBuilder logEmitterBuilder(String instrumentationName);
}
