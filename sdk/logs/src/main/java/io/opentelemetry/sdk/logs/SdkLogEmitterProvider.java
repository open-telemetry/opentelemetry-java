/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.internal.ComponentRegistry;
import io.opentelemetry.sdk.resources.Resource;
import java.io.Closeable;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/** SDK registry for creating {@link LogEmitter}s. */
public final class SdkLogEmitterProvider implements Closeable {

  static final String DEFAULT_EMITTER_NAME = "unknown";
  private static final Logger LOGGER = Logger.getLogger(SdkLogEmitterProvider.class.getName());

  private final LogEmitterSharedState sharedState;
  private final ComponentRegistry<SdkLogEmitter> logEmitterComponentRegistry;

  /**
   * Returns a new {@link SdkLogEmitterProviderBuilder} for {@link SdkLogEmitterProvider}.
   *
   * @return a new builder instance
   */
  public static SdkLogEmitterProviderBuilder builder() {
    return new SdkLogEmitterProviderBuilder();
  }

  SdkLogEmitterProvider(
      Resource resource,
      Supplier<LogLimits> logLimitsSupplier,
      List<LogProcessor> processors,
      Clock clock) {
    this.sharedState = new LogEmitterSharedState(resource, logLimitsSupplier, processors, clock);
    this.logEmitterComponentRegistry =
        new ComponentRegistry<>(
            instrumentationScopeInfo -> new SdkLogEmitter(sharedState, instrumentationScopeInfo));
  }

  /**
   * Gets or creates a named log emitter instance.
   *
   * @param instrumentationScopeName A name uniquely identifying the instrumentation scope, such as
   *     the instrumentation library, package, or fully qualified class name. Must not be null.
   * @return a log emitter instance
   */
  public LogEmitter get(String instrumentationScopeName) {
    return logEmitterBuilder(instrumentationScopeName).build();
  }

  /**
   * Creates a {@link LogEmitterBuilder} instance.
   *
   * @param instrumentationScopeName the name of the instrumentation scope
   * @return a log emitter builder instance
   */
  public LogEmitterBuilder logEmitterBuilder(String instrumentationScopeName) {
    if (instrumentationScopeName == null || instrumentationScopeName.isEmpty()) {
      LOGGER.fine("LogEmitter requested without instrumentation scope name.");
      instrumentationScopeName = DEFAULT_EMITTER_NAME;
    }
    return new SdkLogEmitterBuilder(logEmitterComponentRegistry, instrumentationScopeName);
  }

  /**
   * Request the active log processor to process all logs that have not yet been processed.
   *
   * @return a {@link CompletableResultCode} which is completed when the flush is finished
   */
  public CompletableResultCode forceFlush() {
    return sharedState.getLogProcessor().forceFlush();
  }

  /**
   * Attempt to shut down the active log processor.
   *
   * @return a {@link CompletableResultCode} which is completed when the active log process has been
   *     shut down.
   */
  public CompletableResultCode shutdown() {
    if (sharedState.hasBeenShutdown()) {
      LOGGER.log(Level.WARNING, "Calling shutdown() multiple times.");
      return CompletableResultCode.ofSuccess();
    }
    return sharedState.shutdown();
  }

  @Override
  public void close() {
    shutdown().join(10, TimeUnit.SECONDS);
  }
}
