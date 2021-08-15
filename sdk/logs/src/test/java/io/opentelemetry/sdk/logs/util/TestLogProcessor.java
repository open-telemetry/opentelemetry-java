/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.util;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logs.LogProcessor;
import io.opentelemetry.sdk.logs.data.LogRecord;
import java.util.ArrayList;
import java.util.List;

public class TestLogProcessor implements LogProcessor {
  private final List<LogRecord> records = new ArrayList<>();
  private boolean shutdownCalled = false;
  private int flushes = 0;

  @Override
  public void addLogRecord(LogRecord record) {
    records.add(record);
  }

  @Override
  public CompletableResultCode shutdown() {
    shutdownCalled = true;
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public CompletableResultCode forceFlush() {
    flushes++;
    return CompletableResultCode.ofSuccess();
  }

  public List<LogRecord> getRecords() {
    return records;
  }

  public int getFlushes() {
    return flushes;
  }

  public boolean shutdownHasBeenCalled() {
    return shutdownCalled;
  }
}
