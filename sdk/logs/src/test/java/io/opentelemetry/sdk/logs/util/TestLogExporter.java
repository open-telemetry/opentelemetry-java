/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.util;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logs.data.LogData;
import io.opentelemetry.sdk.logs.export.LogExporter;
import java.util.ArrayList;
import java.util.Collection;
import javax.annotation.Nullable;

public class TestLogExporter implements LogExporter {

  private final ArrayList<LogData> records = new ArrayList<>();
  @Nullable private Runnable onCall = null;
  private int callCount = 0;

  @Override
  public synchronized CompletableResultCode export(Collection<LogData> records) {
    this.records.addAll(records);
    callCount++;
    if (onCall != null) {
      onCall.run();
    }
    return null;
  }

  @Override
  public CompletableResultCode shutdown() {
    return new CompletableResultCode().succeed();
  }

  public synchronized ArrayList<LogData> getRecords() {
    return records;
  }

  public synchronized void setOnCall(@Nullable Runnable onCall) {
    this.onCall = onCall;
  }

  public synchronized int getCallCount() {
    return callCount;
  }
}
