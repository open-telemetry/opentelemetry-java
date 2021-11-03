/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.resources.Resource;
import java.util.List;
import javax.annotation.Nullable;

/**
 * Represents shared state and config between all {@link SdkLogEmitter}s created by the same {@link
 * SdkLogEmitterProvider}.
 */
final class LogEmitterSharedState {
  private final Object lock = new Object();
  private final Resource resource;
  private final LogProcessor logProcessor;
  private final Clock clock;
  @Nullable private volatile CompletableResultCode shutdownResult = null;

  LogEmitterSharedState(Resource resource, List<LogProcessor> logProcessors, Clock clock) {
    this.resource = resource;
    this.logProcessor = LogProcessor.composite(logProcessors);
    this.clock = clock;
  }

  Resource getResource() {
    return resource;
  }

  LogProcessor getLogProcessor() {
    return logProcessor;
  }

  public Clock getClock() {
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
      shutdownResult = logProcessor.shutdown();
      return shutdownResult;
    }
  }
}
