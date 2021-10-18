/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.internal.ComponentRegistry;
import io.opentelemetry.sdk.resources.Resource;
import java.io.Closeable;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/** Provides instances of {@link LogEmitter}. */
public final class LogEmitterProvider implements Closeable {

  private static final Logger LOGGER = Logger.getLogger(LogEmitterProvider.class.getName());
  private static final String DEFAULT_EMITTER_NAME = "unknown";

  private final LogEmitterSharedState sharedState;
  private final ComponentRegistry<LogEmitter> logEmitterComponentRegistry;

  /**
   * Returns a new {@link LogEmitterProviderBuilder} for {@link LogEmitterProvider}.
   *
   * @return a new builder instance
   */
  public static LogEmitterProviderBuilder builder() {
    return new LogEmitterProviderBuilder();
  }

  LogEmitterProvider(Resource resource, List<LogProcessor> processors) {
    this.sharedState = new LogEmitterSharedState(resource, processors);
    this.logEmitterComponentRegistry =
        new ComponentRegistry<>(
            instrumentationLibraryInfo -> new LogEmitter(sharedState, instrumentationLibraryInfo));
  }

  /**
   * Gets or creates a {@link LogEmitter} instance.
   *
   * @param instrumentationName the name of the instrumentation library
   * @return a log emitter instance
   */
  public LogEmitter get(String instrumentationName) {
    return logEmitterBuilder(instrumentationName).build();
  }

  /**
   * Gets or creates a {@link LogEmitter} instance.
   *
   * @param instrumentationName the name of the instrumentation library
   * @param instrumentationVersion the version of the instrumentation library
   * @return a log emitter instance
   */
  public LogEmitter get(String instrumentationName, String instrumentationVersion) {
    return logEmitterBuilder(instrumentationName)
        .setInstrumentationVersion(instrumentationVersion)
        .build();
  }

  /**
   * Creates a {@link LogEmitterBuilder} instance.
   *
   * @param instrumentationName the name of the instrumentation library
   * @return a log emitter builder instance
   */
  public LogEmitterBuilder logEmitterBuilder(@Nullable String instrumentationName) {
    if (instrumentationName == null || instrumentationName.isEmpty()) {
      LOGGER.fine("Meter requested without instrumentation name.");
      instrumentationName = DEFAULT_EMITTER_NAME;
    }
    return new LogEmitterBuilder(logEmitterComponentRegistry, instrumentationName);
  }

  /**
   * Request the active log processor to process all logs that have not yet been processed.
   *
   * @return a {@link CompletableResultCode} which is completed when the flush is finished
   */
  public CompletableResultCode forceFlush() {
    return sharedState.getActiveLogProcessor().forceFlush();
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
