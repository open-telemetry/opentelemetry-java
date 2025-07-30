/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.CompletableResultCode;

/**
 * A {@link LogRecordProcessor} that filters out log records associated with sampled out spans.
 *
 * <p>Log records not tied to any span (invalid span context) are not sampled out.
 */
public final class TraceBasedLogRecordProcessor implements LogRecordProcessor {

  private final LogRecordProcessor delegate;

  TraceBasedLogRecordProcessor(LogRecordProcessor delegate) {
    this.delegate = requireNonNull(delegate, "delegate");
  }

  /**
   * Returns a new {@link TraceBasedLogRecordProcessorBuilder} to construct a {@link
   * TraceBasedLogRecordProcessor}.
   *
   * @param delegate the processor to delegate to
   * @return a new {@link TraceBasedLogRecordProcessorBuilder}
   */
  public static TraceBasedLogRecordProcessorBuilder builder(LogRecordProcessor delegate) {
    return new TraceBasedLogRecordProcessorBuilder(delegate);
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
