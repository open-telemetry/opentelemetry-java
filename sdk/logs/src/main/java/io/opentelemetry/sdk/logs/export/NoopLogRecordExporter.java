/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.export;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import java.util.Collection;

final class NoopLogRecordExporter implements LogRecordExporter {

  private static final LogRecordExporter INSTANCE = new NoopLogRecordExporter();

  static LogRecordExporter getInstance() {
    return INSTANCE;
  }

  @Override
  public CompletableResultCode export(Collection<LogRecordData> logs) {
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public CompletableResultCode flush() {
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public CompletableResultCode shutdown() {
    return CompletableResultCode.ofSuccess();
  }
}
