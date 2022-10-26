/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.CompletableResultCode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Implementation of {@link LogRecordProcessor} that forwards all logs to a list of {@link
 * LogRecordProcessor}s.
 */
final class MultiLogRecordProcessor implements LogRecordProcessor {

  private final List<LogRecordProcessor> logRecordProcessors;
  private final AtomicBoolean isShutdown = new AtomicBoolean(false);

  /**
   * Create a new {@link MultiLogRecordProcessor}.
   *
   * @param logRecordProcessorsList list of log processors to forward logs to
   * @return a multi log processor instance
   */
  static LogRecordProcessor create(List<LogRecordProcessor> logRecordProcessorsList) {
    return new MultiLogRecordProcessor(
        new ArrayList<>(
            Objects.requireNonNull(logRecordProcessorsList, "logRecordProcessorsList")));
  }

  @Override
  public void onEmit(Context context, ReadWriteLogRecord logRecord) {
    for (LogRecordProcessor logRecordProcessor : logRecordProcessors) {
      logRecordProcessor.onEmit(context, logRecord);
    }
  }

  @Override
  public CompletableResultCode shutdown() {
    if (isShutdown.getAndSet(true)) {
      return CompletableResultCode.ofSuccess();
    }
    List<CompletableResultCode> results = new ArrayList<>(logRecordProcessors.size());
    for (LogRecordProcessor logRecordProcessor : logRecordProcessors) {
      results.add(logRecordProcessor.shutdown());
    }
    return CompletableResultCode.ofAll(results);
  }

  @Override
  public CompletableResultCode forceFlush() {
    List<CompletableResultCode> results = new ArrayList<>(logRecordProcessors.size());
    for (LogRecordProcessor logRecordProcessor : logRecordProcessors) {
      results.add(logRecordProcessor.forceFlush());
    }
    return CompletableResultCode.ofAll(results);
  }

  private MultiLogRecordProcessor(List<LogRecordProcessor> logRecordProcessorsList) {
    this.logRecordProcessors = logRecordProcessorsList;
  }
}
