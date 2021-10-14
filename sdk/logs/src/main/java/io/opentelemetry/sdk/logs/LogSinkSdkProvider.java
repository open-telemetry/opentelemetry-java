/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logs.data.LogRecord;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public final class LogSinkSdkProvider {
  private final LogSink logSink = new SdkLogSink();
  private final List<LogProcessor> processors = new ArrayList<>();

  /**
   * Returns a new {@link LogSinkSdkProviderBuilder} for this class.
   *
   * @return a new {@link LogSinkSdkProviderBuilder} for this class.
   */
  static LogSinkSdkProviderBuilder builder() {
    return new LogSinkSdkProviderBuilder();
  }

  LogSinkSdkProvider() {}

  public LogSink get(String instrumentationName, String instrumentationVersion) {
    // Currently there is no differentiation by instrumentation library
    return logSink;
  }

  public void addLogProcessor(LogProcessor processor) {
    processors.add(Objects.requireNonNull(processor, "Processor can not be null"));
  }

  /**
   * Flushes all attached processors.
   *
   * @return result
   */
  public CompletableResultCode forceFlush() {
    final List<CompletableResultCode> processorResults = new ArrayList<>(processors.size());
    for (LogProcessor processor : processors) {
      processorResults.add(processor.forceFlush());
    }
    return CompletableResultCode.ofAll(processorResults);
  }

  /**
   * Shut down of provider and associated processors.
   *
   * @return result
   */
  public CompletableResultCode shutdown() {
    Collection<CompletableResultCode> processorResults = new ArrayList<>(processors.size());
    for (LogProcessor processor : processors) {
      processorResults.add(processor.shutdown());
    }
    return CompletableResultCode.ofAll(processorResults);
  }

  private class SdkLogSink implements LogSink {
    @Override
    public void offer(LogRecord record) {
      for (LogProcessor processor : processors) {
        processor.addLogRecord(record);
      }
    }
  }
}
