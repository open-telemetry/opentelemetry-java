/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import io.opentelemetry.api.logs.LogEmitter;
import io.opentelemetry.api.logs.LogEmitterBuilder;
import io.opentelemetry.api.logs.LogEmitterProvider;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.internal.ComponentRegistry;
import io.opentelemetry.sdk.resources.Resource;
import java.io.Closeable;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/** SDK registry for creating {@link LogEmitter}s. */
public final class SdkLogEmitterProvider implements LogEmitterProvider, Closeable {

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

  SdkLogEmitterProvider(Resource resource, List<LogProcessor> processors, Clock clock) {
    this.sharedState = new LogEmitterSharedState(resource, processors, clock);
    this.logEmitterComponentRegistry =
        new ComponentRegistry<>(
            instrumentationLibraryInfo ->
                new SdkLogEmitter(sharedState, instrumentationLibraryInfo));
  }

  /**
   * Creates a {@link LogEmitterBuilder} instance.
   *
   * @param instrumentationName the name of the instrumentation library
   * @return a log emitter builder instance
   */
  @Override
  public LogEmitterBuilder logEmitterBuilder(String instrumentationName) {
    if (instrumentationName == null || instrumentationName.isEmpty()) {
      LOGGER.fine("LogEmitter requested without instrumentation name.");
      instrumentationName = DEFAULT_EMITTER_NAME;
    }
    return new SdkLogEmitterBuilder(logEmitterComponentRegistry, instrumentationName);
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
