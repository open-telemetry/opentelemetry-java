/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import io.opentelemetry.context.Context;
import java.util.Objects;

final class NoopLogRecordProcessor implements LogRecordProcessor {
  private static final NoopLogRecordProcessor INSTANCE = new NoopLogRecordProcessor();

  static LogRecordProcessor getInstance() {
    return INSTANCE;
  }

  private NoopLogRecordProcessor() {}

  @Override
  public void onEmit(Context context, ReadWriteLogRecord logRecord) {
    Objects.requireNonNull(context, "context");
    Objects.requireNonNull(logRecord, "logRecord");
  }

  @Override
  public String toString() {
    return "NoopLogRecordProcessor";
  }
}
