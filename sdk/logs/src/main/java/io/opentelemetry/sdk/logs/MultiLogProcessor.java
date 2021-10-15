/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logs.data.LogData;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

final class MultiLogProcessor implements LogProcessor {

  private final List<LogProcessor> logProcessors;
  private final AtomicBoolean isShutdown = new AtomicBoolean(false);

  static LogProcessor create(List<LogProcessor> logProcessorsList) {
    return new MultiLogProcessor(
        new ArrayList<>(Objects.requireNonNull(logProcessorsList, "logProcessorsList")));
  }

  @Override
  public void process(LogData logData) {
    for (LogProcessor logProcessor : logProcessors) {
      logProcessor.process(logData);
    }
  }

  @Override
  public CompletableResultCode shutdown() {
    if (isShutdown.getAndSet(true)) {
      return CompletableResultCode.ofSuccess();
    }
    List<CompletableResultCode> results = new ArrayList<>(logProcessors.size());
    for (LogProcessor logProcessor : logProcessors) {
      results.add(logProcessor.shutdown());
    }
    return CompletableResultCode.ofAll(results);
  }

  @Override
  public CompletableResultCode forceFlush() {
    List<CompletableResultCode> results = new ArrayList<>(logProcessors.size());
    for (LogProcessor logProcessor : logProcessors) {
      results.add(logProcessor.forceFlush());
    }
    return CompletableResultCode.ofAll(results);
  }

  private MultiLogProcessor(List<LogProcessor> logProcessorsList) {
    this.logProcessors = logProcessorsList;
  }
}
