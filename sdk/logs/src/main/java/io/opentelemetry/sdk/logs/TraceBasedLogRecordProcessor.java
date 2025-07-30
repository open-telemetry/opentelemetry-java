/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.CompletableResultCode;
import java.util.List;

/**
 * A {@link LogRecordProcessor} that filters out log records associated with
 * sampled out spans.
 * 
 * Log records not tied to any span (invalid span context) are not sampled out.
 */
public final class TraceBasedLogRecordProcessor implements LogRecordProcessor {

  private final LogRecordProcessor delegate;

  TraceBasedLogRecordProcessor(List<LogRecordProcessor> processors) {
    requireNonNull(processors, "processors");
    this.delegate = LogRecordProcessor.composite(processors);
  }

  /**
   * Returns a new {@link TraceBasedLogRecordProcessorBuilder} to construct a {@link
   * TraceBasedLogRecordProcessor}.
   *
   * @return a new {@link TraceBasedLogRecordProcessorBuilder}
   */
  public static TraceBasedLogRecordProcessorBuilder builder() {
    return new TraceBasedLogRecordProcessorBuilder();
  }

  @Override
  public void onEmit(Context context, ReadWriteLogRecord logRecord) {
    if (logRecord.getSpanContext().isValid() && !logRecord.getSpanContext().isSampled()) {
      return;
    }
    delegate.onEmit(context, logRecord);
  }

  @Override
  public CompletableResultCode shutdown() {
    return delegate.shutdown();
  }

  @Override
  public CompletableResultCode forceFlush() {
    return delegate.forceFlush();
  }

  @Override
  public String toString() {
    return "TraceBasedLogRecordProcessor{" + "delegate=" + delegate + '}';
  }
}
