/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

final class NoopLogRecordProcessor implements LogRecordProcessor {
  private static final NoopLogRecordProcessor INSTANCE = new NoopLogRecordProcessor();

  static LogRecordProcessor getInstance() {
    return INSTANCE;
  }

  private NoopLogRecordProcessor() {}

  @Override
  public void onEmit(ReadWriteLogRecord logRecord) {}
}
