/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.api.logs.Severity;

/** Builder class for {@link SeverityBasedLogRecordProcessor}. */
public final class SeverityBasedLogRecordProcessorBuilder {

  private final Severity minimumSeverity;
  private final LogRecordProcessor delegate;

  SeverityBasedLogRecordProcessorBuilder(Severity minimumSeverity, LogRecordProcessor delegate) {
    this.minimumSeverity = requireNonNull(minimumSeverity, "minimumSeverity");
    this.delegate = requireNonNull(delegate, "delegate");
  }

  /**
   * Returns a new {@link SeverityBasedLogRecordProcessor} with the configuration of this builder.
   *
   * @return a new {@link SeverityBasedLogRecordProcessor}
   */
  public SeverityBasedLogRecordProcessor build() {
    return new SeverityBasedLogRecordProcessor(minimumSeverity, delegate);
  }
}
