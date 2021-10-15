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

public final class SdkLogSinkProvider implements Closeable {
  private static final Logger logger = Logger.getLogger(SdkLogSinkProvider.class.getName());
  private final LogSinkSharedState sharedState;
  private final ComponentRegistry<SdkLogSink> logSinkSdkComponentRegistry;

  public static LogSinkSdkProviderBuilder builder() {
    return new LogSinkSdkProviderBuilder();
  }

  SdkLogSinkProvider(Resource resource, List<LogProcessor> processors) {
    this.sharedState = new LogSinkSharedState(resource, processors);
    this.logSinkSdkComponentRegistry =
        new ComponentRegistry<>(
            instrumentationLibraryInfo -> new SdkLogSink(sharedState, instrumentationLibraryInfo));
  }

  /**
   * Create a log sink instance.
   *
   * @param instrumentationName the name of the instrumentation library
   * @return a log sink instance
   */
  public LogSink get(String instrumentationName) {
    return logSinkBuilder(instrumentationName).build();
  }

  /**
   * Create a log sink instance.
   *
   * @param instrumentationName the name of the instrumentation library
   * @param instrumentationVersion the version of the instrumentation library
   * @return a log sink instance
   */
  public LogSink get(String instrumentationName, String instrumentationVersion) {
    return logSinkBuilder(instrumentationName)
        .setInstrumentationVersion(instrumentationVersion)
        .build();
  }

  /**
   * Create a log sink builder.
   *
   * @param instrumentationName the name of the instrumentation library
   * @return a log sink builder instance
   */
  public SdkLogSinkBuilder logSinkBuilder(String instrumentationName) {
    // TODO: should instrumentationName be nullable
    return new SdkLogSinkBuilder(logSinkSdkComponentRegistry, instrumentationName);
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
      logger.log(Level.WARNING, "Calling shutdown() multiple times.");
      return CompletableResultCode.ofSuccess();
    }
    return sharedState.shutdown();
  }

  @Override
  public void close() {
    shutdown().join(10, TimeUnit.SECONDS);
  }
}
