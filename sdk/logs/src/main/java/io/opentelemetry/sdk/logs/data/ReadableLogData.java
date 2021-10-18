/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import javax.annotation.concurrent.Immutable;

@AutoValue
@Immutable
public abstract class ReadableLogData implements LogData {

  ReadableLogData() {}

  /**
   * Create a {@link ReadableLogData} instance.
   *
   * @param resource the resource
   * @param instrumentationLibraryInfo the instrumentation library info
   * @param logRecord the log record
   * @return the instance
   */
  public static ReadableLogData create(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      LogRecord logRecord) {
    return new AutoValue_ReadableLogData(
        logRecord.getEpochNanos(),
        logRecord.getTraceId(),
        logRecord.getSpanId(),
        logRecord.getFlags(),
        logRecord.getSeverity(),
        logRecord.getSeverityText(),
        logRecord.getName(),
        logRecord.getBody(),
        logRecord.getAttributes(),
        resource,
        instrumentationLibraryInfo);
  }
}
