/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import static java.util.Objects.requireNonNull;

/** Builder class for {@link TraceBasedLogRecordProcessor}. */
public final class TraceBasedLogRecordProcessorBuilder {

  private final LogRecordProcessor delegate;

  TraceBasedLogRecordProcessorBuilder(LogRecordProcessor delegate) {
    this.delegate = requireNonNull(delegate, "delegate");
  }

  /**
   * Returns a new {@link TraceBasedLogRecordProcessor} with the configuration of this builder.
   *
   * @return a new {@link TraceBasedLogRecordProcessor}
   */
  public TraceBasedLogRecordProcessor build() {
    return new TraceBasedLogRecordProcessor(delegate);
  }
}
