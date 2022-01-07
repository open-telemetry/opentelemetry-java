/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.export;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logs.data.LogData;
import java.util.Collection;

final class NoopLogExporter implements LogExporter {

  private static final LogExporter INSTANCE = new NoopLogExporter();

  static LogExporter getInstance() {
    return INSTANCE;
  }

  @Override
  public CompletableResultCode export(Collection<LogData> logs) {
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
