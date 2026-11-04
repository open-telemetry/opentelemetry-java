/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.internal.ExceptionAttributeResolver;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.resources.internal.SdkResourceProvider;
import java.util.function.Supplier;
import javax.annotation.Nullable;

/**
 * Represents shared state and config between all {@link SdkLogger}s created by the same {@link
 * SdkLoggerProvider}.
 */
final class LoggerSharedState {
  private final Object lock = new Object();
  private final SdkResourceProvider resourceProvider;
  private final Supplier<LogLimits> logLimitsSupplier;
  private final LogRecordProcessor logRecordProcessor;
  private final Clock clock;
  private final ExceptionAttributeResolver exceptionAttributeResolver;
  private final SdkLoggerInstrumentation loggerInstrumentation;
  @Nullable private volatile CompletableResultCode shutdownResult = null;

  LoggerSharedState(
      SdkResourceProvider resourceProvider,
      Supplier<LogLimits> logLimitsSupplier,
      LogRecordProcessor logRecordProcessor,
      Clock clock,
      ExceptionAttributeResolver exceptionAttributeResolver,
      SdkLoggerInstrumentation loggerInstrumentation) {
    this.resourceProvider = resourceProvider;
    this.logLimitsSupplier = logLimitsSupplier;
    this.logRecordProcessor = logRecordProcessor;
    this.clock = clock;
    this.exceptionAttributeResolver = exceptionAttributeResolver;
    this.loggerInstrumentation = loggerInstrumentation;
  }

  Resource getResource() {
    return resourceProvider.getResource();
  }

  LogLimits getLogLimits() {
    return logLimitsSupplier.get();
  }

  LogRecordProcessor getLogRecordProcessor() {
    return logRecordProcessor;
  }

  Clock getClock() {
    return clock;
  }

  ExceptionAttributeResolver getExceptionAttributeResolver() {
    return exceptionAttributeResolver;
  }

  SdkLoggerInstrumentation getLoggerInstrumentation() {
    return loggerInstrumentation;
  }

  boolean hasBeenShutdown() {
    return shutdownResult != null;
  }

  CompletableResultCode shutdown() {
    synchronized (lock) {
      if (shutdownResult != null) {
        return shutdownResult;
      }
      shutdownResult = logRecordProcessor.shutdown();
      return shutdownResult;
    }
  }
}
