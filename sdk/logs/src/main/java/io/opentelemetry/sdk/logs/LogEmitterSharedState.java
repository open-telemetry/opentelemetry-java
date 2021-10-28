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
 * Represents shared state and config between all {@link SdkLogEmitter}s created by the same {@link
 * SdkLogEmitterProvider}.
 */
final class LogEmitterSharedState {
  private final Object lock = new Object();
  private final Resource resource;
  private final LogProcessor logProcessor;
  @Nullable private volatile CompletableResultCode shutdownResult = null;

  LogEmitterSharedState(Resource resource, List<LogProcessor> logProcessors) {
    this.resource = resource;
    this.logProcessor = LogProcessor.composite(logProcessors);
  }

  Resource getResource() {
    return resource;
  }

  LogProcessor getLogProcessor() {
    return logProcessor;
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
