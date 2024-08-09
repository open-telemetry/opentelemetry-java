/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.logs;

import io.opentelemetry.api.internal.IncubatingUtil;
import javax.annotation.concurrent.ThreadSafe;

/**
 * A registry for creating scoped {@link Logger}s. The name <i>Provider</i> is for consistency with
 * other languages and it is <b>NOT</b> loaded using reflection.
 *
 * <p>The OpenTelemetry logs bridge API exists to enable bridging logs from other log frameworks
 * (e.g. SLF4J, Log4j, JUL, Logback, etc) into OpenTelemetry and is <b>NOT</b> a replacement log
 * API.
 *
 * @since 1.27.0
 * @see Logger
 */
@ThreadSafe
public interface LoggerProvider {

  /**
   * Gets or creates a named Logger instance.
   *
   * @param instrumentationScopeName A name uniquely identifying the instrumentation scope, such as
   *     the instrumentation library, package, or fully qualified class name. Must not be null.
   * @return a Logger instance.
   */
  default Logger get(String instrumentationScopeName) {
    return loggerBuilder(instrumentationScopeName).build();
  }

  /**
   * Creates a LoggerBuilder for a named Logger instance.
   *
   * @param instrumentationScopeName A name uniquely identifying the instrumentation scope, such as
   *     the instrumentation library, package, or fully qualified class name. Must not be null.
   * @return a LoggerBuilder instance.
   */
  LoggerBuilder loggerBuilder(String instrumentationScopeName);

  /** Returns a no-op {@link LoggerProvider} which provides Loggers which do not record or emit. */
  static LoggerProvider noop() {
    return IncubatingUtil.incubatingApiIfAvailable(
        DefaultLoggerProvider.getInstance(),
        "io.opentelemetry.api.incubator.logs.ExtendedDefaultLoggerProvider");
  }
}
