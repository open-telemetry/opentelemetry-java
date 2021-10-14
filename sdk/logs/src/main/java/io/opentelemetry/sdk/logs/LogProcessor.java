/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logs.data.LogRecord;
import io.opentelemetry.sdk.trace.SdkTracerProvider;

public interface LogProcessor {

  void addLogRecord(LogRecord record);

  /**
   * Called when {@link SdkTracerProvider#shutdown()} is called.
   *
   * @return result
   */
  CompletableResultCode shutdown();

  /**
   * Processes all span events that have not yet been processed.
   *
   * @return result
   */
  CompletableResultCode forceFlush();
}
