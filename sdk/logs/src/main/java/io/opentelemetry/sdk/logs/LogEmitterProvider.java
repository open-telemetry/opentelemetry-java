/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import javax.annotation.Nullable;

/** A registry for creating {@link LogEmitter}s. */
public interface LogEmitterProvider {

  /**
   * Gets or creates a {@link LogEmitter} instance.
   *
   * @param instrumentationName the name of the instrumentation library
   * @return a log emitter instance
   */
  LogEmitter get(String instrumentationName);

  /**
   * Gets or creates a {@link LogEmitter} instance.
   *
   * @param instrumentationName the name of the instrumentation library
   * @param instrumentationVersion the version of the instrumentation library
   * @return a log emitter instance
   */
  LogEmitter get(String instrumentationName, String instrumentationVersion);

  /**
   * Creates a {@link LogEmitterBuilder} instance.
   *
   * @param instrumentationName the name of the instrumentation library
   * @return a log emitter builder instance
   */
  LogEmitterBuilder logEmitterBuilder(@Nullable String instrumentationName);
}
