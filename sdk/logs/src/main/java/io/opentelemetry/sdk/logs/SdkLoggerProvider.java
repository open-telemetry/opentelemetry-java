/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.logs.LoggerBuilder;
import io.opentelemetry.api.logs.LoggerProvider;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.internal.ComponentRegistry;
import io.opentelemetry.sdk.resources.Resource;
import java.io.Closeable;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Level;
import javax.annotation.Nullable;

/** SDK implementation for {@link LoggerProvider}. */
public final class SdkLoggerProvider implements LoggerProvider, Closeable {

  static final String DEFAULT_LOGGER_NAME = "unknown";
  private static final java.util.logging.Logger LOGGER =
      java.util.logging.Logger.getLogger(SdkLoggerProvider.class.getName());

  private final LoggerSharedState sharedState;
  private final ComponentRegistry<SdkLogger> loggerComponentRegistry;
  private final boolean isNoopLogRecordProcessor;

  /**
   * Returns a new {@link SdkLoggerProviderBuilder} for {@link SdkLoggerProvider}.
   *
   * @return a new builder instance
   */
  public static SdkLoggerProviderBuilder builder() {
    return new SdkLoggerProviderBuilder();
  }

  SdkLoggerProvider(
      Resource resource,
      Supplier<LogLimits> logLimitsSupplier,
      List<LogRecordProcessor> processors,
      Clock clock) {
    LogRecordProcessor logRecordProcessor = LogRecordProcessor.composite(processors);
    this.sharedState =
        new LoggerSharedState(resource, logLimitsSupplier, logRecordProcessor, clock);
    this.loggerComponentRegistry =
        new ComponentRegistry<>(
            instrumentationScopeInfo -> new SdkLogger(sharedState, instrumentationScopeInfo));
    this.isNoopLogRecordProcessor = logRecordProcessor instanceof NoopLogRecordProcessor;
  }

  @Override
  public Logger get(String instrumentationScopeName) {
    return loggerComponentRegistry.get(
        instrumentationNameOrDefault(instrumentationScopeName), null, null, Attributes.empty());
  }

  @Override
  public LoggerBuilder loggerBuilder(String instrumentationScopeName) {
    if (isNoopLogRecordProcessor) {
      return LoggerProvider.noop().loggerBuilder(instrumentationScopeName);
    }
    return new SdkLoggerBuilder(
        loggerComponentRegistry, instrumentationNameOrDefault(instrumentationScopeName));
  }

  private static String instrumentationNameOrDefault(@Nullable String instrumentationScopeName) {
    if (instrumentationScopeName == null || instrumentationScopeName.isEmpty()) {
      LOGGER.fine("Logger requested without instrumentation scope name.");
      return DEFAULT_LOGGER_NAME;
    }
    return instrumentationScopeName;
  }

  /**
   * Request the active log processor to process all logs that have not yet been processed.
   *
   * @return a {@link CompletableResultCode} which is completed when the flush is finished
   */
  public CompletableResultCode forceFlush() {
    return sharedState.getLogRecordProcessor().forceFlush();
  }

  /**
   * Attempt to shut down the active log processor.
   *
   * @return a {@link CompletableResultCode} which is completed when the active log process has been
   *     shut down.
   */
  public CompletableResultCode shutdown() {
    if (sharedState.hasBeenShutdown()) {
      LOGGER.log(Level.INFO, "Calling shutdown() multiple times.");
      return CompletableResultCode.ofSuccess();
    }
    return sharedState.shutdown();
  }

  @Override
  public void close() {
    shutdown().join(10, TimeUnit.SECONDS);
  }

  @Override
  public String toString() {
    return "SdkLoggerProvider{"
        + "clock="
        + sharedState.getClock()
        + ", resource="
        + sharedState.getResource()
        + ", logLimits="
        + sharedState.getLogLimits()
        + ", logRecordProcessor="
        + sharedState.getLogRecordProcessor()
        + '}';
  }
}
