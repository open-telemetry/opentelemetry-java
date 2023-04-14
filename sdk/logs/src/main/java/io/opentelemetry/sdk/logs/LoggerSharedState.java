/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.resources.Resource;
import java.util.function.Supplier;
import javax.annotation.Nullable;

/**
 * Represents shared state and config between all {@link SdkLogger}s created by the same {@link
 * SdkLoggerProvider}.
 */
final class LoggerSharedState {
  private final Object lock = new Object();
  private final Resource resource;
  private final Supplier<LogLimits> logLimitsSupplier;
  private final LogRecordProcessor logRecordProcessor;
  private final Clock clock;
  @Nullable private volatile CompletableResultCode shutdownResult = null;

  LoggerSharedState(
      Resource resource,
      Supplier<LogLimits> logLimitsSupplier,
      LogRecordProcessor logRecordProcessor,
      Clock clock) {
    this.resource = resource;
    this.logLimitsSupplier = logLimitsSupplier;
    this.logRecordProcessor = logRecordProcessor;
    this.clock = clock;
  }

  Resource getResource() {
    return resource;
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
