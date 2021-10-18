/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.resources.Resource;
import java.util.List;
import javax.annotation.Nullable;

/**
 * Represents shared state and config between all {@link LogEmitter}s created by the same {@link
 * LogEmitterProvider}.
 */
final class LogEmitterSharedState {
  private final Object lock = new Object();
  private final Resource resource;
  private final LogProcessor activeLogProcessor;
  @Nullable private volatile CompletableResultCode shutdownResult = null;

  LogEmitterSharedState(Resource resource, List<LogProcessor> logProcessors) {
    this.resource = resource;
    this.activeLogProcessor = LogProcessor.composite(logProcessors);
  }

  Resource getResource() {
    return resource;
  }

  LogProcessor getActiveLogProcessor() {
    return activeLogProcessor;
  }

  boolean hasBeenShutdown() {
    return shutdownResult != null;
  }

  CompletableResultCode shutdown() {
    synchronized (lock) {
      if (shutdownResult != null) {
        return shutdownResult;
      }
      shutdownResult = activeLogProcessor.shutdown();
      return shutdownResult;
    }
  }
}
