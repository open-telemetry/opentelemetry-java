/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.logs.LoggerBuilder;
import io.opentelemetry.api.logs.LoggerProvider;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.internal.ComponentRegistry;
import io.opentelemetry.sdk.internal.ExceptionAttributeResolver;
import io.opentelemetry.sdk.internal.ScopeConfigurator;
import io.opentelemetry.sdk.logs.internal.LoggerConfig;
import io.opentelemetry.sdk.resources.Resource;
import java.io.Closeable;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Level;
import javax.annotation.Nullable;

/**
 * SDK implementation for {@link LoggerProvider}.
 *
 * @since 1.27.0
 */
public final class SdkLoggerProvider implements LoggerProvider, Closeable {

  static final String DEFAULT_LOGGER_NAME = "unknown";
  private static final java.util.logging.Logger LOGGER =
      java.util.logging.Logger.getLogger(SdkLoggerProvider.class.getName());

  private final LoggerSharedState sharedState;
  private final ComponentRegistry<SdkLogger> loggerComponentRegistry;
  private final boolean isNoopLogRecordProcessor;

  // deliberately not volatile because of performance concerns
  // - which means its eventually consistent
  private ScopeConfigurator<LoggerConfig> loggerConfigurator;

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
      Clock clock,
      ScopeConfigurator<LoggerConfig> loggerConfigurator,
      ExceptionAttributeResolver exceptionAttributeResolver,
      Supplier<MeterProvider> meterProvider) {
    LogRecordProcessor logRecordProcessor = LogRecordProcessor.composite(processors);
    this.sharedState =
        new LoggerSharedState(
            resource,
            logLimitsSupplier,
            logRecordProcessor,
            clock,
            exceptionAttributeResolver,
            new SdkLoggerInstrumentation(meterProvider));
    this.loggerComponentRegistry =
        new ComponentRegistry<>(
            instrumentationScopeInfo ->
                SdkLogger.create(
                    sharedState,
                    instrumentationScopeInfo,
                    getLoggerConfig(instrumentationScopeInfo)));
    this.loggerConfigurator = loggerConfigurator;
    this.isNoopLogRecordProcessor = logRecordProcessor instanceof NoopLogRecordProcessor;
  }

  private LoggerConfig getLoggerConfig(InstrumentationScopeInfo instrumentationScopeInfo) {
    LoggerConfig loggerConfig = loggerConfigurator.apply(instrumentationScopeInfo);
    return loggerConfig == null ? LoggerConfig.defaultConfig() : loggerConfig;
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
   * Updates the logger configurator, which computes {@link LoggerConfig} for each {@link
   * InstrumentationScopeInfo}.
   *
   * <p>This method is experimental so not public. You may reflectively call it using {@link
   * io.opentelemetry.sdk.logs.internal.SdkLoggerProviderUtil#setLoggerConfigurator(SdkLoggerProvider,
   * ScopeConfigurator)}.
   *
   * @see LoggerConfig#configuratorBuilder()
   */
  void setLoggerConfigurator(ScopeConfigurator<LoggerConfig> loggerConfigurator) {
    this.loggerConfigurator = loggerConfigurator;
    this.loggerComponentRegistry
        .getComponents()
        .forEach(
            sdkLogger ->
                sdkLogger.updateLoggerConfig(
                    getLoggerConfig(sdkLogger.getInstrumentationScopeInfo())));
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
        + ", loggerConfigurator="
        + loggerConfigurator
        + '}';
  }
}
