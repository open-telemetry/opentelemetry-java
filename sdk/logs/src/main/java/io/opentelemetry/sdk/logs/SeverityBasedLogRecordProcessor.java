/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.CompletableResultCode;
import java.util.List;

/**
 * Implementation of {@link LogRecordProcessor} that filters log records based on minimum severity
 * level and delegates to downstream processors.
 *
 * <p>Only log records with severity greater than or equal to the configured minimum are forwarded.
 */
public final class SeverityBasedLogRecordProcessor implements LogRecordProcessor {

  private final Severity minimumSeverity;
  private final LogRecordProcessor delegate;

  SeverityBasedLogRecordProcessor(Severity minimumSeverity, List<LogRecordProcessor> processors) {
    this.minimumSeverity = requireNonNull(minimumSeverity, "minimumSeverity");
    requireNonNull(processors, "processors");
    this.delegate = LogRecordProcessor.composite(processors);
  }

  /**
   * Returns a new {@link SeverityBasedLogRecordProcessorBuilder} to construct a {@link
   * SeverityBasedLogRecordProcessor}.
   *
   * @param minimumSeverity the minimum severity level required for processing
   * @return a new {@link SeverityBasedLogRecordProcessorBuilder}
   */
  public static SeverityBasedLogRecordProcessorBuilder builder(Severity minimumSeverity) {
    return new SeverityBasedLogRecordProcessorBuilder(minimumSeverity);
  }

  @Override
  public void onEmit(Context context, ReadWriteLogRecord logRecord) {
    if (logRecord.getSeverity().getSeverityNumber() >= minimumSeverity.getSeverityNumber()) {
      delegate.onEmit(context, logRecord);
    }
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
    return "SeverityBasedLogRecordProcessor{"
        + "minimumSeverity="
        + minimumSeverity
        + ", delegate="
        + delegate
        + '}';
  }
}
